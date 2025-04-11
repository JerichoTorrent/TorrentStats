# 🎯 TorrentStats

TorrentStats is a modular Minecraft paper plugin for tracking player statistics across your server network. Built to support a wide variety of popular plugins (like mcMMO, EvenMoreFish, Vault, and more), it centralizes all relevant data into a MySQL or SQLite database for use in web dashboards, leaderboards, player profiles, etc.

---

## 🔧 Features

- 🧮 Tracks a select few core Bukkit stats (e.g., kills, blocks mined, distance walked)
- 🪙 Economy tracking with Vault support
- 🎣 Legendary fish tracking via [EvenMoreFish](https://www.spigotmc.org/resources/evenmorefish-%E2%96%AA-extensive-fishing-plugin-%E2%96%AA.91310/)
- 💸 Shop & profit tracking via [EzChestShop](https://github.com/nouish/EzChestShop)
- 🧪 A hacky way of tracking [XP bottles](https://www.spigotmc.org/resources/expbottle-withdraw-your-xp-into-bottles-1-15-1-21.98763/)
- 📖 Tracks completed quests from [BeautyQuests](https://www.spigotmc.org/resources/beautyquests.39255/)
- 🏢 Plot tracker using [PlotSquared](https://www.spigotmc.org/resources/plotsquared-v7.77506/)
- 💼 Job XP and level tracking via [AdvancedJobs](https://www.spigotmc.org/resources/1-17-1-21-5-%E2%AD%95-advancedjobs-%E2%AD%90-20-default-jobs-create-your-own-jobs-plugin%E2%9A%A1gui-editor-%E2%9C%85.114936/updates)
- 👤 Team info via [BetterTeams](https://www.spigotmc.org/resources/better-teams.17129/)
- 🧠 Skill XP and level tracking via [McMMO](https://www.spigotmc.org/resources/official-mcmmo-original-author-returns.64348/)
- 📈 Modular plugin support (enable/disable hooks in `config.yml`)
- 🛠️ Supports MySQL or SQLite backends
- ⚒️ Designed for use with websites, leaderboards, and web profiles

---

## 📦 Supported Hooks

- [x] **mcMMO**
- [x] **EvenMoreFish**
- [x] **Vault**
- [x] **EzChestShop**
- [x] **AdvancedJobs**
- [x] **PlotSquared**
- [x] **BeautyQuests**
- [x] **BetterTeams**
- [x] **FlightControl**
- [x] **ExpBottle**
- [x] **Bukkit Stats**
- [ ] **SkyFactionsReborn** *(coming soon)*
- [ ] **TheDungeons** *(planned)*

---

## 🧠 Requirements

- Java 17+
- Minecraft server (Paper or compatible fork)
- (Optional) MySQL/MariaDB for persistent stats across servers

---

## 📂 Database Tables

TorrentStats creates and manages the following tables automatically:

- `player_stats`
- `player_jobs`
- `player_skills`
- `player_team_stats`

Each table has a server name column for splitting up data on networks.

---

## 🛠 Configuration

After installing the plugin and running the server once, a `config.yml` will be generated. Here's a sample:

```yaml
#  ______                           __  _____ __        __      
# /_  __/___  _____________  ____  / /_/ ___// /_____ _/ /______
#  / / / __ \/ ___/ ___/ _ \/ __ \/ __/\__ \/ __/ __ `/ __/ ___/
# / / / /_/ / /  / /  /  __/ / / / /_ ___/ / /_/ /_/ / /_(__  ) 
#/_/  \____/_/  /_/   \___/_/ /_/\__//____/\__/\__,_/\__/____/  
                                                               
# This is the per-server configuration for TorrentStats: https://github.com/JerichoTorrent/TorrentStats

# The server name is unique; this will be how your split up stats from each server across your network.
# If you want to share statistics, use a remote database (MySQL, MariaDB, etc) for the plugin hook/listener.
server-name: Survival

# Enable plugin hooks. If the plugin is not detected at run-time, it will ignore it. If the plugin fails to
# start, report it on Github.
enabled-hooks:
  mcmmo: true
  evenmorefish: true
  vault: true
  ezchestshop: true
  flightcontrol: true
  thedungeons: true
  betterteams: true
  plotsquared: true
  expbottle: true
  beautyquests: true
  bukkitstats: true # Tracks a select few important stats from https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Statistic.html

# Input your MySQL database credentials here. If you're using SQLite, just change the type to sqlite and leave the rest alone.
database:
  type: mysql # or sqlite
  host: localhost
  port: 3306
  name: torrent_stats
  user: root
  password: password

# Your web URL is the base URL for your website.
web-url: "https://torrentsmp.com" # or your dev env URL
```

---

## 🔌 Commands  

`/login` - Generates a link code to connect your Minecraft account with your website profile. See -> <https://github.com/JerichoTorrent/TorrentWeb>
`/torrentstats <debug|reload>` - Shows debug info or reload the config file.

---

## 🏗️ Building  

> Requires Maven  
1. Clone the repo and compile:
```sh
git clone https://github.com/JerichoTorrent/TorrentStats.git
cd TorrentStats
mvn clean package
```
The compiled .jar will be found in target/TorrentStats-<version>.jar

---

## 💡 Web Integration  

TorrentStats is built to power:
- Player profile pages
- Gamemode leaderboards
- Personal dashboards
- Server stats and xp/leveling rewards

---

## 🙌 Contributing  

Want to add support for a new plugin? Open a PR or issue! Hooks are modular and easy to extend.

---

## 🌐 Credit  

Developed for [Torrent Network](https://discord.gg/torrent). IP: `torrentsmp.com`