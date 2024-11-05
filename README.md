# XraySuspect

**XraySuspect** integrates with Paper’s anti-xray system to dynamically apply anti-xray measures only to players exhibiting suspicious mining behavior. This approach seeks to limit the overhead caused by anti-xray mechanics by selectively refreshing chunk packets and applying anti-xray to specific players, rather than to the entire player base.

## ⚠️ Important Considerations

1. **Performance Implications**  
   This plugin is designed with resource optimization in mind, though the benefits depend heavily on server context. Anti-xray measures typically impact about 20% of players in active mining scenarios; if this percentage increases significantly, refreshing chunks for multiple players could introduce performance costs that outweigh the benefits.

   In high-stress environments or larger servers, it’s essential to test and monitor performance impacts. While permission checks (e.g., for anti-xray bypasses) are generally lightweight with efficient permissions plugins like LuckPerms, issues such as packet flooding(caused by refreshed chunks) can still affect server performance.

2. **Anti-Xray Evasion Concerns**  
   While anti-xray mechanisms can deter most players, savvy players may still work around obfuscation (e.g., by using custom mods). As this plugin sends unobfuscated chunk packets initially, players might reject the refreshed chunk packets (with anti-xray on) and continue x-ray mining undeterred. Consider this when using the plugin.

3. **Compatibility with Third-Party Anti-Xray**  
   XraySuspect may reduce overhead when using Paper’s native anti-xray. However, using additional anti-xray plugins, such as [Stonar's RayTraceAntiXray](https://github.com/stonar96/RayTraceAntiXray), may increase resource demands. Compatibility testing in realistic server conditions is recommended, as combining these plugins may produce mixed performance outcomes.

4. **Testing for Optimal Server Load**  
   Testing under actual player conditions on a high-activity server is recommended to accurately assess the plugin’s impact and effectiveness.

5. **Discouragement as the Best Anti-Xray Strategy**  
   The most effective approach against x-ray use often involves discouraging players from cheating rather than relying solely on technical anti-xray measures. This can include setting clear rules, providing incentives for legitimate play, and educating players about the effects of cheating on the game experience. Anti-xray plugins and mechanics work best as supplementary/last resort tools rather than the primary method of prevention.

## Installation

1. Enable anti-xray permissions for configured worlds in Paper’s settings.
2. Place the plugin in your Paper server's `plugins` folder.
3. Add the anti-xray bypass permission to the default player group, allowing the plugin to toggle the bypass dynamically.
4. Configure the plugin to set mining thresholds and chunk refresh behaviors suited to your server's load tolerance and player base.

## Contributing

Your contributions are welcome! If you’d like to suggest enhancements, report issues, or submit code, please open an issue or pull request.

## Roadmap

1. Create an optimized default configuration based on typical usage scenarios.
2. Implement checks based on each player’s view distance rather than the server’s global view distance to reduce redundant data loading and improve compatibility with various client configurations.
