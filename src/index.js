import { NativeModules, Platform } from 'react-native';

const { ExpoTopwiseSdkPrinter } = NativeModules;

if (!ExpoTopwiseSdkPrinter) {
  console.error("ExpoTopwiseSdkPrinter native module is not available. Make sure you've properly configured the native module.");
}

/**
 * Expo module for Topwise SDK Printer
 */
class TopwisePrinter {
  /**
   * Check if the printer is available
   * @returns {Promise<boolean>} True if the printer is available
   */
  static isAvailable() {
    return Platform.OS === 'android' ? ExpoTopwiseSdkPrinter.isAvailable() : Promise.resolve(false);
  }

  /**
   * Print text
   * @param {string} text - Text to print
   * @param {Object} options - Print options
   * @param {number} options.fontSize - Font size (default: 24)
   * @param {string} options.align - Alignment ('LEFT', 'CENTER', 'RIGHT')
   * @returns {Promise<boolean>} True if printing was successful
   */
  static printText(text, options = {}) {
    if (Platform.OS !== 'android') {
      console.warn('TopwisePrinter is only available on Android');
      return Promise.resolve(false);
    }
    
    return ExpoTopwiseSdkPrinter.printText(
      text, 
      options.fontSize || 24, 
      options.align || 'LEFT'
    );
  }

  /**
   * Print image
   * @param {string} uri - Image URI to print
   * @param {Object} options - Print options
   * @param {string} options.align - Alignment ('LEFT', 'CENTER', 'RIGHT')
   * @returns {Promise<boolean>} True if printing was successful
   */
  static printImage(uri, options = {}) {
    if (Platform.OS !== 'android') {
      console.warn('TopwisePrinter is only available on Android');
      return Promise.resolve(false);
    }
    
    return ExpoTopwiseSdkPrinter.printImage(uri, options.align || 'CENTER');
  }

  /**
   * Print QR code
   * @param {string} data - Data to encode in QR code
   * @param {Object} options - Print options
   * @param {number} options.size - QR code size (default: 200)
   * @param {string} options.align - Alignment ('LEFT', 'CENTER', 'RIGHT') 
   * @returns {Promise<boolean>} True if printing was successful
   */
  static printQRCode(data, options = {}) {
    if (Platform.OS !== 'android') {
      console.warn('TopwisePrinter is only available on Android');
      return Promise.resolve(false);
    }
    
    return ExpoTopwiseSdkPrinter.printQRCode(
      data, 
      options.size || 200, 
      options.align || 'CENTER'
    );
  }

  /**
   * Print receipt template
   * @param {Object} data - Receipt data
   * @param {string} data.header - Header text
   * @param {string} data.footer - Footer text
   * @param {Array<{label: string, value: string}>} data.items - Line items
   * @returns {Promise<boolean>} True if printing was successful
   */
  static printReceipt(data) {
    if (Platform.OS !== 'android') {
      console.warn('TopwisePrinter is only available on Android');
      return Promise.resolve(false);
    }
    
    return ExpoTopwiseSdkPrinter.printReceipt(data);
  }

  /**
   * Feed paper
   * @param {number} lines - Number of lines to feed (default: 3)
   * @returns {Promise<boolean>} True if successful
   */
  static feedPaper(lines = 3) {
    if (Platform.OS !== 'android') {
      console.warn('TopwisePrinter is only available on Android');
      return Promise.resolve(false);
    }
    
    return ExpoTopwiseSdkPrinter.feedPaper(lines);
  }

  /**
   * Get printer status
   * @returns {Promise<Object>} Printer status information
   */
  static getStatus() {
    if (Platform.OS !== 'android') {
      console.warn('TopwisePrinter is only available on Android');
      return Promise.resolve({ available: false, error: 'Platform not supported' });
    }
    
    return ExpoTopwiseSdkPrinter.getPrinterStatus();
  }

  /**
   * Cut paper (if supported by device)
   * @returns {Promise<boolean>} True if successful
   */
  static cutPaper() {
    if (Platform.OS !== 'android') {
      console.warn('TopwisePrinter is only available on Android');
      return Promise.resolve(false);
    }
    
    return ExpoTopwiseSdkPrinter.cutPaper();
  }
}

export default TopwisePrinter;
