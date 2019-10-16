/*********************************************************************
 Adafruit invests time and resources providing this open source code,
 please support Adafruit and open-source hardware by purchasing
 products from Adafruit!

 MIT license, check LICENSE for more information
 All text above, and the splash screen below must be included in
 any redistribution
*********************************************************************/
#include <bluefruit.h>
#include <Adafruit_LittleFS.h>
#include <InternalFileSystem.h>

const int buttonPin = A0;
int buttonState = 0;
int prevButtonState = 0;
bool conStopped = false;
bool wasConnected = false;
uint16_t conn_id = 0;

// BLE Service
BLEDfu  bledfu;  // OTA DFU service
BLEDis  bledis;  // device information
BLEUart bleuart; // uart over ble
BLEBas  blebas;  // battery

uint8_t textBeat=0;

/**
* <h1>e_Float beacon software for BLE Adafruit devices</h1>
* This software is used to run the beacon software system, this code is an adaped version
* of an example provided by Adafruit to include connection and disconnect functionality driven
* by a button press.
*	
* @author David Fitzsimmons
* @version 1.2
* @since 2019-9-28
*/

void setup()
{
  Serial.begin(115200);
    // for nrf52840 with native usb
  
  // Setup the BLE LED to be enabled on CONNECT
  // Note: This is actually the default behaviour, but provided
  // here in case you want to control this LED manually via PIN 19
  Bluefruit.autoConnLed(true);

  // Config the peripheral connection with maximum bandwidth 
  // more SRAM required by SoftDevice
  // Note: All config***() function must be called before begin()
  Bluefruit.configPrphBandwidth(BANDWIDTH_MAX);

  Bluefruit.begin();
  //supported tx_power values
  //-40dBm, -20dBm, -16dBm, -12dBm, -16dBm, -12dBm, -8dBm, -4dBm, 0dBm, +3dBm and +4dBm
  Bluefruit.setTxPower(4);    
  Bluefruit.setName("eFloat");
  Bluefruit.Periph.setConnectCallback(connect_callback);
  Bluefruit.Periph.setDisconnectCallback(disconnect_callback);

  Serial.println(Bluefruit.getAppearance());

  // To be consistent OTA DFU should be added first if it exists
  bledfu.begin();

  // Configure and Start Device Information Service
  bledis.setManufacturer("Adafruit Industries");
  bledis.setModel("Bluefruit Feather52");
  bledis.begin();

  // Configure and Start BLE Uart Service
  bleuart.begin();
  
  // Start BLE Battery Service
  blebas.begin();
  blebas.write(100);

  // Set up and start advertising
  startAdv();

  Serial.println("Please use Adafruit's Bluefruit LE app to connect in UART mode");
  Serial.println("Once connected, enter character(s) that you wish to send");

  //initializing the pushButton pin as an input
  pinMode(buttonPin, INPUT);
}

void startAdv(void)
{
  // Advertising packet
  Bluefruit.Advertising.addFlags(BLE_GAP_ADV_FLAGS_LE_ONLY_GENERAL_DISC_MODE);
  Bluefruit.Advertising.addTxPower();

  // Include bleuart 128-bit uuid
  Bluefruit.Advertising.addService(bleuart);

  // Secondary Scan Response packet (optional)
  // Since there is no room for 'Name' in Advertising packet
  Bluefruit.ScanResponse.addName();
  
  /* Start Advertising
   * - Enable auto advertising if disconnected
   * - Interval:  fast mode = 20 ms, slow mode = 152.5 ms
   * - Timeout for fast mode is 30 seconds
   * - Start(timeout) with timeout = 0 will advertise forever (until connected)
   * 
   * For recommended advertising interval
   * https://developer.apple.com/library/content/qa/qa1931/_index.html   
   */
  Bluefruit.Advertising.restartOnDisconnect(false);
  Bluefruit.Advertising.setInterval(32, 244);    // in unit of 0.625 ms
  Bluefruit.Advertising.setFastTimeout(30);      // number of seconds in fast mode
  Bluefruit.Advertising.start(0);                // 0 = Don't stop advertising after n seconds  
}

/**
 * stopping the BLE advertising process so the device cannot be rediscovered
 */
void stopAdv(void) 
{
  Serial.println("Stopping Advertisment...");
  Bluefruit.Advertising.stop();
  if (!Bluefruit.Advertising.isRunning()) 
  {
    Serial.println("Advertising successfully stopped");  
  }
}

/**
 * stopping the BLE connection, forcing the device to disconnect from the beacon
 */
void stopCon(void) 
{
  Serial.println("Stopping Connection...");
  Bluefruit.disconnect(conn_id);
  if (!Bluefruit.connected())
  {
    Serial.println("device successfully disconnected");  
  }  
  else
  {
    Serial.println("device failed to disconnect");  
  }
}

/**
 * procedure to restart advertising
 */
void restartAdv(void)
{
  Serial.println("Restarting Advertising");
  Bluefruit.Advertising.start(0);  
}

void loop()
{ 
  //reading the state of the pushbutton pin
  buttonState = digitalRead(buttonPin);
  
  // Forward data from HW Serial to BLEUART
  while (Serial.available())
  {
    // Delay to wait for enough input, since we have a limited transmission buffer
    delay(2);

    uint8_t buf[64];
    int count = Serial.readBytes(buf, sizeof(buf));
    bleuart.write( buf, count );
  }

  // Forward from BLEUART to HW Serial
  while ( bleuart.available() )
  {
    uint8_t ch;
    ch = (uint8_t) bleuart.read();
    Serial.write(ch);

  }

  //reading the button input and the disconnected the device from the host
  if (buttonState == HIGH && buttonState != prevButtonState) {
    prevButtonState = buttonState;
    Serial.println("button pressed");
    if (Bluefruit.Periph.connected()) 
    {    
      stopAdv();
      stopCon();
      conStopped = true;
    }
  } 
  else if (buttonState == LOW && buttonState != prevButtonState) 
  {
    prevButtonState = buttonState;
    Serial.println("button released");
    if (conStopped) 
    {
      restartAdv();
      conStopped = false;  
    }
  }
}

// callback invoked when central connects
void connect_callback(uint16_t conn_handle)
{
  //prevent the device from reconnecting while the conStopped button is triggered
  if (!conStopped) {
    // Get the reference to current connection
    BLEConnection* connection = Bluefruit.Connection(conn_handle);
  
    char central_name[32] = { 0 };
    connection->getPeerName(central_name, sizeof(central_name));
  
    Serial.print("Connected to ");
    Serial.println(central_name);
  }

  conn_id = conn_handle;
}

/**
 * Callback invoked when a connection is dropped
 * @param conn_handle connection where this event happens
 * @param reason is a BLE_HCI_STATUS_CODE which can be found in ble_hci.h
 */
void disconnect_callback(uint16_t conn_handle, uint8_t reason)
{
  (void) conn_handle;
  (void) reason;

  Serial.println(conn_handle);
  Serial.print("Disconnected, reason = 0x"); 
  Serial.println(reason, HEX);
}
