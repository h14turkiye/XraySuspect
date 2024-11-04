# XraySuspect

**XraySuspect** is designed to integrate with Paper's anti-xray mechanisms, effectively utilizing server resources to monitor and flag suspicious x-ray activities, while restricting Paper's anti-xray functionality to identified suspects.

## Warning

While this plugin *may* optimize resource usage, please note that it is not guaranteed(Complicated stuff about `use-permission`). Paper's AntiXray can increase network usage, and performance impacts from AntiXray modules, such as [Stonar's RayTraceAntiXray](https://github.com/stonar96/RayTraceAntiXray), can be significant. The most effective anti-xray strategies often focus on discouraging players from using cheats rather than relying solely on AntiXray measures.

Be cautious, as exposing ores to players might enable them to exploit seed anti-xray strategies, even if anti-xray features are later enabled. It is advisable to implement additional protective measures against such exploits.

## Installation

1. Ensure that the permission for world anti-xray configuration is enabled.
2. Download the plugin and place it in your Paper server's `plugins` folder.
3. Add the Paper anti-xray bypass permission to your default group to activate functionality.
4. Configure the plugin according to your server's specific needs.

## Contributing

Contributions are welcome! If you have suggestions, improvements, or bug fixes, please submit a pull request or open an issue.

## TODO

1. Create a good default configuration.
