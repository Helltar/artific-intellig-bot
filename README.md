# AI Bot for Telegram

This bot is focused on Group Chats.

## Installation

### Docker Compose

```bash
mkdir aibot && cd aibot && \
wget https://raw.githubusercontent.com/Helltar/artific-intellig-bot/master/{.env,compose.yaml}
```

Edit the **.env** file with the following:

- `CREATOR_ID`: Your Telegram user-ID (You can use [@artific_intellig_bot](https://t.me/artific_intellig_bot) by entering the command `/myid`)
- `BOT_TOKEN`: Obtain from [BotFather](https://t.me/BotFather)
- `BOT_USERNAME`: Obtain from [BotFather](https://t.me/BotFather) (Example: artific_intellig_bot)

Also include PostgreSQL connection data.

```bash
docker compose up -d
```

## Usage

### Obtain API Keys

First, get the following API key:

- [OpenAI API Key](https://platform.openai.com/api-keys)

Add it using the command in the bot:

- `/updatekey sk-qwerty...`

### Commands

- `/chat` - Chat and analyze images
- `/imgen` - Generate images from text prompts

### Additional Chat Commands

- `/chatctx` - View dialogue history
- `/chatrm` - Clear history

### Admin Commands

#### Update Models

- `/chatmodel` - Change OpenAI model for chat and vision
- `/imgmodel` - Change model for image generation

#### Change Command State

- `/enable commandName` (Example: `/enable chat`)
- `/disable commandName` (Example: `/disable imgen`)

> **NOTE:** Run `/enable` or `/disable` with no arguments to view supported commands.

#### Ban User

- `/ban` (Use as reply to a user message, Example: `/ban reason`)
- `/unban` (Use as reply to user message or by user ID)
- `/banlist`

#### Slowmode

- `/slowmode` (Default: 10 requests per hour per user)

#### Manage Admins

- `/addadmin` (Add admin by ID, Example: `/addadmin 123456789 username`)
- `/rmadmin` (Remove admin by ID)
- `/sudoers` (View an admin list)

#### Manage Chats

- `/addchat` (Add chat to allowlist, Use in chat or by ID)
- `/rmchat` (Remove chat from allowlist, Use in chat or by ID)
- `/chats` (View a chat list)
