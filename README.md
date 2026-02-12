# AI Bot for Telegram

This bot is designed for use in group chats.

## Installation

### Docker Compose

```bash
mkdir aibot && cd aibot && \
wget https://raw.githubusercontent.com/Helltar/artific-intellig-bot/master/{.env,compose.yaml}
```

Edit the **.env** file and set the following values:

- `CREATOR_ID`: your Telegram user ID (for example, `1234567890`)
- `BOT_TOKEN`: get it from [BotFather](https://t.me/BotFather)
- `BOT_USERNAME`: get it from [BotFather](https://t.me/BotFather) (for example, `artific_intellig_bot`)

Also add your PostgreSQL connection settings.

```bash
docker compose up -d
```

## Usage

### Get API keys

First, create the following API key:

- [OpenAI API Key](https://platform.openai.com/api-keys)

Then add it in the bot with:

- `/updatekey sk-qwerty...`

### Commands

- `/chat` - chat and analyze images
- `/imgen` - generate images from text prompts

### Additional chat commands

- `/chatctx` - view dialogue history
- `/chatrm` - clear history

### Admin commands

#### Update models

- `/chatmodel` - change the OpenAI model for chat and vision
- `/imgmodel` - change the model for image generation

#### Change command state

- `/enable commandName` (example: `/enable chat`)
- `/disable commandName` (example: `/disable imgen`)

> **Note:** Run `/enable` or `/disable` without arguments to view supported commands.

#### Ban users

- `/ban` (use as a reply to a user message, example: `/ban reason`)
- `/unban` (use as a reply to a user message or by user ID)
- `/banlist`

#### Slow mode

- `/slowmode` (default: 10 requests per hour per user)

#### Manage admins

- `/addadmin` (add an admin by ID, example: `/addadmin 123456789 username`)
- `/rmadmin` (remove an admin by ID)
- `/sudoers` (view the admin list)

#### Manage chats

- `/addchat` (add a chat to the allowlist; use it in the chat or by ID)
- `/rmchat` (remove a chat from the allowlist; use it in the chat or by ID)
- `/chats` (view the chat list)
