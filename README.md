# Expo Topwise SDK Printer

A React Native module for Expo that enables printing functionality with Topwise POS devices.

## Installation

```bash
npm install expo-topwise-sdk-printer
# or
yarn add expo-topwise-sdk-printer
```

## Requirements

- Expo SDK 45 or newer
- Android only (iOS is not supported as Topwise SDK is Android-specific)

## Setup

This package is compatible with the Expo managed workflow with EAS Build system.

### Configure for Android

Add the following lines to your app.json/app.config.js:

```json
{
  "expo": {
    "plugins": [
      "expo-topwise-sdk-printer"
    ]
  }
}
```

## Usage

```javascript
import TopwisePrinter from 'expo-topwise-sdk-printer';

// Check if printer is available
const checkPrinterAvailable = async () => {
  try {
    const available = await TopwisePrinter.isAvailable();
    console.log('Printer available:', available);
    return available;
  } catch (error) {
    console.error('Error checking printer:', error);
    return false;
  }
};

// Print text
const printText = async () => {
  try {
    await TopwisePrinter.printText('Hello World!', {
      fontSize: 24,
      align: 'CENTER'
    });
    console.log('Text printed successfully');
  } catch (error) {
    console.error('Error printing text:', error);
  }
};

// Print QR code
const printQRCode = async () => {
  try {
    await TopwisePrinter.printQRCode('https://example.com', {
      size: 200,
      align: 'CENTER'
    });
    console.log('QR code printed successfully');
  } catch (error) {
    console.error('Error printing QR code:', error);
  }
};

// Print receipt
const printReceipt = async () => {
  try {
    await TopwisePrinter.printReceipt({
      header: 'RECEIPT',
      items: [
        { label: 'Item 1', value: '$10.00' },
        { label: 'Item 2', value: '$20.00' },
        { label: 'Total', value: '$30.00' }
      ],
      footer: 'Thank you for your purchase!'
    });
    console.log('Receipt printed successfully');
  } catch (error) {
    console.error('Error printing receipt:', error);
  }
};
```

## API Reference

### `isAvailable()`

Checks if the printer is available.

**Returns:** `Promise<boolean>` - True if the printer is available.

### `printText(text, options)`

Prints text.

**Parameters:**
- `text` (string): Text to print.
- `options` (object, optional):
  - `fontSize` (number): Font size (default: 24).
  - `align` (string): Alignment ('LEFT', 'CENTER', 'RIGHT') (default: 'LEFT').

**Returns:** `Promise<boolean>` - True if printing was successful.

### `printImage(uri, options)`

Prints an image.

**Parameters:**
- `uri` (string): Image URI to print.
- `options` (object, optional):
  - `align` (string): Alignment ('LEFT', 'CENTER', 'RIGHT') (default: 'CENTER').

**Returns:** `Promise<boolean>` - True if printing was successful.

### `printQRCode(data, options)`

Prints a QR code.

**Parameters:**
- `data` (string): Data to encode in QR code.
- `options` (object, optional):
  - `size` (number): QR code size (default: 200).
  - `align` (string): Alignment ('LEFT', 'CENTER', 'RIGHT') (default: 'CENTER').

**Returns:** `Promise<boolean>` - True if printing was successful.

### `printReceipt(data)`

Prints a receipt template.

**Parameters:**
- `data` (object):
  - `header` (string): Header text.
  - `footer` (string): Footer text.
  - `items` (array): Line items with the following structure:
    - `label` (string): Item label.
    - `value` (string): Item value.

**Returns:** `Promise<boolean>` - True if printing was successful.

### `feedPaper(lines)`

Feeds paper.

**Parameters:**
- `lines` (number, optional): Number of lines to feed (default: 3).

**Returns:** `Promise<boolean>` - True if successful.

### `getStatus()`

Gets printer status.

**Returns:** `Promise<object>` - Printer status information.

### `cutPaper()`

Cuts paper (if supported by device).

**Returns:** `Promise<boolean>` - True if successful.

## Original Topwise SDK

This module is based on the Topwise SDK printer functionality, adapted for use with Expo and React Native.

## License

MIT