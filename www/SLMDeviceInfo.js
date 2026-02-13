var exec = require('cordova/exec');

var SLMDeviceInfo = {

    /**
     * Obtiene información completa del dispositivo.
     * @param {Function} successCallback - Recibe objeto con:
     *   {
     *     uuid: string,            // Identificador único del dispositivo
     *     model: string,           // Modelo (ej: "iPhone14,5", "SM-G991B")
     *     manufacturer: string,    // Fabricante (ej: "Apple", "Samsung")
     *     platform: string,        // "iOS" o "Android"
     *     osVersion: string,       // Versión del OS (ej: "17.2", "14")
     *     deviceName: string,      // Nombre del dispositivo
     *     isPhysicalDevice: boolean,// true si es dispositivo real, false si emulador
     *     screenWidth: number,     // Ancho pantalla en px
     *     screenHeight: number,    // Alto pantalla en px
     *     screenScale: number,     // Escala de pantalla (iOS) / densidad (Android)
     *     totalMemory: number,     // RAM total en MB
     *     processorCount: number   // Número de procesadores/cores
     *   }
     * @param {Function} errorCallback - Recibe string con mensaje de error
     */
    getDeviceInfo: function (successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'SLMDeviceInfo', 'getDeviceInfo', []);
    },

    /**
     * Obtiene información de la batería.
     * @param {Function} successCallback - Recibe objeto con:
     *   {
     *     level: number,       // Nivel de batería 0.0 - 1.0
     *     isCharging: boolean  // true si está cargando
     *   }
     * @param {Function} errorCallback - Recibe string con mensaje de error
     */
    getBatteryInfo: function (successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'SLMDeviceInfo', 'getBatteryInfo', []);
    },

    /**
     * Obtiene información de la red.
     * @param {Function} successCallback - Recibe objeto con:
     *   {
     *     connectionType: string,  // "wifi", "cellular", "none", "unknown"
     *     isConnected: boolean,    // true si hay conexión activa
     *     carrierName: string      // Nombre del operador celular (si aplica)
     *   }
     * @param {Function} errorCallback - Recibe string con mensaje de error
     */
    getNetworkInfo: function (successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'SLMDeviceInfo', 'getNetworkInfo', []);
    }
};

module.exports = SLMDeviceInfo;
