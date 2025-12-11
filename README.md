# 💰 Aconomy

A simple and flexible economy plugin for [AllayMC](https://github.com/AllayMC/Allay) that implements the [EconomyAPI](https://github.com/AllayMC/EconomyAPI).

## ✨ Features

- 🔌 Full implementation of EconomyAPI interfaces
- 💾 Multiple storage backends (JSON, SQLite, H2)
- 💵 Configurable currency (name, symbol, decimal places)

## 📋 Requirements

- Java 21 or higher
- AllayMC server
- [EconomyAPI](https://github.com/AllayMC/EconomyAPI) plugin installed

## 📥 Installation

1. Download the latest release from the [Releases](https://github.com/smartcmd/Aconomy/releases) page
2. Place the jar file in your server's `plugins` folder
3. Start the server
4. Configure the plugin in `plugins/Aconomy/config.yml`

## ⚙️ Configuration

```yaml
# Storage Configuration
storage:
  # Storage type: json, sqlite, or h2
  # json - Uses local JSON file (accounts.json)
  # sqlite - Uses SQLite database (economy.db)
  # h2 - Uses H2 database (economy.mv.db)
  type: json

# Currency Configuration
currency:
  # The name of the currency (singular form)
  name: Coin
  # The plural form of the currency name
  plural: Coins
  # The symbol displayed before amounts
  symbol: "$"
  # Number of decimal places to display
  fraction_digits: 2

# Economy Settings
economy:
  # Default balance for new accounts
  default_balance: 0.0
```

## 📜 Commands

All commands use `/aconomy` (alias: `/aco`)

| Command                           | Description                      | Permission                 |
|-----------------------------------|----------------------------------|----------------------------|
| `/aco balance [player]`           | Check balance                    | `aconomy.command.balance`  |
| `/aco transfer <amount> <player>` | Transfer money to another player | `aconomy.command.transfer` |
| `/aco top [count]`                | Show richest players             | `aconomy.command.top`      |
| `/aco set <amount> [player]`      | Set player's balance             | `aconomy.command.set`      |
| `/aco deposit <amount> [player]`  | Add money to account             | `aconomy.command.deposit`  |
| `/aco withdraw <amount> [player]` | Remove money from account        | `aconomy.command.withdraw` |

### 🔐 Permissions

| Permission                 | Description             | Default  |
|----------------------------|-------------------------|----------|
| `aconomy.command`          | Base command permission | Everyone |
| `aconomy.command.balance`  | Check balance           | Everyone |
| `aconomy.command.transfer` | Transfer money          | Everyone |
| `aconomy.command.top`      | View leaderboard        | Everyone |
| `aconomy.command.set`      | Set balance (admin)     | OP only  |
| `aconomy.command.deposit`  | Deposit money (admin)   | OP only  |
| `aconomy.command.withdraw` | Withdraw money (admin)  | OP only  |

## 📄 License

This project is licensed under the LGPL-3.0 License - see the [LICENSE](LICENSE) file for details.

## 🙏 Credits

- [AllayMC](https://github.com/AllayMC/Allay) - The Minecraft Bedrock server software
- [EconomyAPI](https://github.com/AllayMC/EconomyAPI) - The economy API interface
