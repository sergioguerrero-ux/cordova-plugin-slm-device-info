import UIKit
import CoreTelephony
import SystemConfiguration

@objc(SLMDeviceInfo) class SLMDeviceInfo: CDVPlugin {

    // MARK: - getDeviceInfo

    @objc(getDeviceInfo:)
    func getDeviceInfo(command: CDVInvokedUrlCommand) {
        commandDelegate.run {
            let device = UIDevice.current
            let screen = UIScreen.main
            let processInfo = ProcessInfo.processInfo

            var info: [String: Any] = [
                "uuid": device.identifierForVendor?.uuidString ?? "unknown",
                "model": self.getDeviceModel(),
                "manufacturer": "Apple",
                "platform": "iOS",
                "osVersion": device.systemVersion,
                "deviceName": device.name,
                "isPhysicalDevice": !self.isSimulator(),
                "screenWidth": Int(screen.bounds.width * screen.scale),
                "screenHeight": Int(screen.bounds.height * screen.scale),
                "screenScale": screen.scale,
                "totalMemory": Int(processInfo.physicalMemory / (1024 * 1024)),
                "processorCount": processInfo.processorCount
            ]

            let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: info)
            self.commandDelegate.send(result, callbackId: command.callbackId)
        }
    }

    // MARK: - getBatteryInfo

    @objc(getBatteryInfo:)
    func getBatteryInfo(command: CDVInvokedUrlCommand) {
        let device = UIDevice.current
        device.isBatteryMonitoringEnabled = true

        let level = device.batteryLevel
        let state = device.batteryState

        let isCharging = (state == .charging || state == .full)

        let info: [String: Any] = [
            "level": level >= 0 ? level : -1,
            "isCharging": isCharging
        ]

        let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: info)
        commandDelegate.send(result, callbackId: command.callbackId)
    }

    // MARK: - getNetworkInfo

    @objc(getNetworkInfo:)
    func getNetworkInfo(command: CDVInvokedUrlCommand) {
        commandDelegate.run {
            let connectionType = self.getConnectionType()
            let isConnected = (connectionType != "none")
            let carrierName = self.getCarrierName()

            let info: [String: Any] = [
                "connectionType": connectionType,
                "isConnected": isConnected,
                "carrierName": carrierName
            ]

            let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: info)
            self.commandDelegate.send(result, callbackId: command.callbackId)
        }
    }

    // MARK: - Helpers

    private func getDeviceModel() -> String {
        var systemInfo = utsname()
        uname(&systemInfo)
        let modelCode = withUnsafePointer(to: &systemInfo.machine) {
            $0.withMemoryRebound(to: CChar.self, capacity: 1) {
                String(cString: $0)
            }
        }
        return modelCode
    }

    private func isSimulator() -> Bool {
        #if targetEnvironment(simulator)
        return true
        #else
        return false
        #endif
    }

    private func getConnectionType() -> String {
        var zeroAddress = sockaddr_in()
        zeroAddress.sin_len = UInt8(MemoryLayout.size(ofValue: zeroAddress))
        zeroAddress.sin_family = sa_family_t(AF_INET)

        guard let reachability = withUnsafePointer(to: &zeroAddress, {
            $0.withMemoryRebound(to: sockaddr.self, capacity: 1) {
                SCNetworkReachabilityCreateWithAddress(nil, $0)
            }
        }) else {
            return "unknown"
        }

        var flags = SCNetworkReachabilityFlags()
        if !SCNetworkReachabilityGetFlags(reachability, &flags) {
            return "unknown"
        }

        let isReachable = flags.contains(.reachable)
        let needsConnection = flags.contains(.connectionRequired)

        if !isReachable || needsConnection {
            return "none"
        }

        if flags.contains(.isWWAN) {
            return "cellular"
        }

        return "wifi"
    }

    private func getCarrierName() -> String {
        let networkInfo = CTTelephonyNetworkInfo()
        if #available(iOS 12.0, *) {
            if let carriers = networkInfo.serviceSubscriberCellularProviders {
                for (_, carrier) in carriers {
                    if let name = carrier.carrierName {
                        return name
                    }
                }
            }
        } else {
            if let carrier = networkInfo.subscriberCellularProvider,
               let name = carrier.carrierName {
                return name
            }
        }
        return "unknown"
    }
}
