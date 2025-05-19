const { withAndroidManifest } = require('@expo/config-plugins');

/**
 * Expo Config Plugin for Topwise SDK Printer
 * This plugin adds the necessary permissions and configurations to the Android manifest
 */
const withTopwiseSdkPrinter = (config) => {
  return withAndroidManifest(config, async (config) => {
    const androidManifest = config.modResults;
    
    // Add Topwise SDK permissions to the manifest
    const permissions = [
      'android.permission.CLOUDPOS_PRINTER',
      'android.permission.WRITE_EXTERNAL_STORAGE',
      'android.permission.READ_EXTERNAL_STORAGE',
      'android.permission.INTERNET',
      // Add more permissions as needed
    ];
    
    // Check if we need to add the permissions
    const manifestPermissions = androidManifest.manifest['uses-permission'] || [];
    
    // Add new permissions if they don't exist yet
    permissions.forEach((permission) => {
      const permissionName = `android.permission.${permission.split('.').pop()}`;
      const exists = manifestPermissions.some(
        (p) => p.$?.['android:name'] === permissionName || p.$?.['android:name'] === permission
      );
      
      if (!exists) {
        manifestPermissions.push({
          $: {
            'android:name': permission,
          },
        });
      }
    });
    
    // Update the manifest with new permissions
    androidManifest.manifest['uses-permission'] = manifestPermissions;
    
    return config;
  });
};

module.exports = withTopwiseSdkPrinter;
