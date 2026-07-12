---
name: danted-proxy-config
description: Configuration pattern for Dante SOCKS proxy (danted) with split access: free for VPN, authenticated for Yggdrasil
source: auto-skill
extracted_at: '2026-07-01T22:37:57.330Z'
---

# Dante SOCKS Proxy (danted) Configuration Pattern

This skill documents the approach for configuring danted with split access policies: free access for VPN clients, strong authentication required for Yggdrasil network clients.

## Architecture Overview

**danted** is a SOCKS v4/v5 proxy daemon. The configuration uses:
- **Client rules**: Control which source IPs can connect to the proxy
- **SOCKS rules**: Control which source IPs can make SOCKS requests (with/without auth)
- **Method specification**: Global `socksmethod` must include all methods used in rules

## Key Configuration Principles

### 1. Interface Binding
```conf
internal: <interface> port = <port>
```
Bind to specific interfaces (VPN, Yggdrasil, localhost) rather than 0.0.0.0.

### 2. Global Method Declaration
```conf
socksmethod: username none
```
Must declare ALL authentication methods used in any rule globally.

### 3. Rule Ordering Matters
Rules are evaluated top-to-bottom. Place specific rules before general ones:
1. Specific auth-required rules (Yggdrasil with username)
2. No-auth rules (VPN, localhost)
3. Catch-all block rules

### 4. Client vs SOCKS Rules
- `client pass/block`: Connection-level access (who can connect to proxy port)
- `socks pass/block`: SOCKS request-level access (who can make proxy requests)

## Working Configuration Template

```conf
logoutput: /var/log/socks.log
internal: <vpn_interface> port = 61125
internal: <yggdrasil_interface> port = 61125
internal: 127.0.0.1 port = 61125
external: <outbound_interface>
clientmethod: none
socksmethod: username none
user.privileged: root
user.notprivileged: nobody

# Client access - allow both networks to connect
client pass { from: 200::/7 to: 0.0.0.0/0 log: error connect disconnect }
client pass { from: 192.168.55.0/24 to: 0.0.0.0/0 log: error connect disconnect }
client pass { from: 127.0.0.1/32 to: 0.0.0.0/0 log: error connect disconnect }
client block { from: 0.0.0.0/0 to: 0.0.0.0/0 log: error connect disconnect }

# SOCKS access - VPN/localhost free, Yggdrasil requires auth
socks pass { from: 127.0.0.1/32 to: 0.0.0.0/0 socksmethod: username command: bind connect udpassociate log: error connect disconnect }
socks pass { from: 127.0.0.1/32 to: 0.0.0.0/0 socksmethod: none command: bind connect udpassociate log: error connect disconnect }
socks pass { from: 192.168.55.0/24 to: 0.0.0.0/0 socksmethod: none command: bind connect udpassociate log: error connect disconnect }
socks pass { from: 200::/7 to: 0.0.0.0/0 socksmethod: username command: bind connect udpassociate log: error connect disconnect }
socks block { from: 0.0.0.0/0 to: 0.0.0.0/0 log: error connect disconnect }
```

## Testing Checklist

| Source | Auth | Expected | Test Command |
|--------|------|----------|--------------|
| 127.0.0.1 | none | ✅ Allow | `curl --socks5 127.0.0.1:61125 http://ifconfig.me/ip` |
| 127.0.0.1 | user:pass | ✅ Allow | `curl --socks5 user:pass@127.0.0.1:61125 http://ifconfig.me/ip` |
| 192.168.55.x | none | ✅ Allow | `curl --socks5 192.168.55.1:61125 http://ifconfig.me/ip` |
| 200::/7 (Yggdrasil) | user:pass | ✅ Allow | `curl --socks5 user:pass@[Yggdrasil_IP]:61125 http://ifconfig.me/ip` |
| 200::/7 (Yggdrasil) | none | ❌ Block | `curl --socks5 [Yggdrasil_IP]:61125 http://ifconfig.me/ip` |
| Other IPs | any | ❌ Block | N/A |

## Common Issues & Fixes

| Issue | Cause | Fix |
|-------|-------|-----|
| "method 'none' not in global socksmethod" | Rule uses `socksmethod: none` but global doesn't include `none` | Add `none` to global `socksmethod: username none` |
| "syntax error near token" | Invalid config syntax (stray chars, missing braces) | Run `danted -f /etc/danted.conf -V` to validate |
| "Address already in use" | Another danted instance running | `systemctl stop danted` before manual test |
| Auth fails from localhost | Rule order: no-auth rule matches before auth rule | Put `socksmethod: username` rule BEFORE `socksmethod: none` for same source |

## Authentication Setup

```bash
# Create dedicated SOCKS user (no shell, no home)
useradd -r -s /usr/sbin/nologin -M -d /nonexistent socksuser

# Set strong password
echo "socksuser:StrongPassw0rd!2024" | chpasswd
```

## Service Management

```bash
# Validate config
danted -f /etc/danted.conf -V

# Debug run (foreground)
danted -f /etc/danted.conf -d 3 -N 1

# Service control
systemctl restart danted
systemctl status danted
journalctl -u danted -f
```

## Security Notes

- Use strong passwords for Yggdrasil auth (20+ chars, mixed case, numbers, symbols)
- Restrict `user.privileged` to root only
- Log all connections (`log: error connect disconnect`) for audit
- Block all other networks by default
- Consider firewall rules as additional layer