App({
  globalData: {
    // 真机 / 预览：填运行后端的电脑在「当前 Wi‑Fi」下的 IPv4（PowerShell: ipconfig，选 WLAN 对应地址）。
    // 不要用 VMware/VirtualBox 虚拟网卡（常见 192.168.x.1）。开发者工具模拟器可用 http://127.0.0.1:8080/api。
    // 须勾选「详情 → 本地设置 → 不校验合法域名、web-view、TLS…」，且本机防火墙放行 8080。
    apiBase: 'http://192.168.181.37:8080/api'
  }
});
