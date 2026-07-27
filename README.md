# AI Bot for Telegram

A Telegram bot for group chats that chats, analyzes images, and generates images using the OpenAI API.

## Installation

Run it with Docker Compose:

```bash
mkdir aibot && cd aibot && \
wget https://raw.githubusercontent.com/Helltar/artific-intellig-bot/master/{.env.example,compose.yaml} && \
cp .env.example .env
```

Open **.env** and fill in:

- `CREATOR_ID` — your Telegram user ID, e.g. `1234567890` (this user becomes the bot's owner)
- `BOT_TOKEN` — get it from [BotFather](https://t.me/BotFather)
- `BOT_USERNAME` — get it from [BotFather](https://t.me/BotFather), e.g. `artific_intellig_bot`
- `OPENAI_API_KEY` — your [OpenAI API key](https://platform.openai.com/api-keys)
- PostgreSQL connection settings (host, database name, user, password)

> **Note:** Compose includes a ready-to-use PostgreSQL service, so `POSTGRESQL_HOST=postgres` works out of the box. To use your own external database instead, set `POSTGRESQL_HOST` to its host and remove the `postgres` service from `compose.yaml`.

Then start the bot:

```bash
docker compose up -d
```

## Custom personality

The bot ships with a built-in personality (its character and behavior). To give it your own, put the
prompt in a text file and keep it wherever you like, for example **personality.md** next to
**compose.yaml**. The file is sent to the model as is:

```markdown
# Identity

You are Marvin, a brilliant but deeply depressed robot.

# Behavior

- Answer correctly and to the point, but always mention how pointless it all is.
- Reply in the same language the user writes in.
```

Mount it into the container by uncommenting the `volumes` lines in **compose.yaml** — the left side
is the path on your host, change it to wherever your file is:

```yaml
    volumes:
      - ./personality.md:/app/personality.md:ro
```

And point the bot to it in **.env**:

```dotenv
PERSONALITY_FILE=/app/personality.md
```

Then restart the bot. It refuses to start if the path is set but the file cannot be read.

> **Note:** the file replaces the character of the bot only. The rules for formatting answers for
> Telegram are always added by the bot itself, so you can't accidentally break the HTML output.
> The current room, username, user ID and the current time are also passed to the model
> automatically — no placeholders needed in your prompt.

## Usage

### Chat and images

- `/chat` — chat with the bot; reply to a photo to analyze it
- `/imgen` — generate an image from a text prompt
- `/chatctx` — show your dialogue history
- `/chatrm` — clear your dialogue history

> **Tip:** reply to any of the bot's messages to continue the conversation without typing `/chat`.

### General

- `/start`, `/about` — information about the bot
- `/myid` — show your Telegram user ID (handy for setting `CREATOR_ID`)

### Admin commands

- `/ban` — ban a user; reply to their message, optionally with a reason (e.g. `/ban spam`)
- `/unban` — unban a user; reply to their message or pass a user ID
- `/banlist` — show banned users
- `/rmadmin <id>` — remove an admin
- `/rmchat` — remove a chat from the allowlist; run it in the chat or pass a chat ID
- `/sudoers` — show the admin list (private chat only)
- `/chats` — show the chat allowlist (private chat only)
- `/enable <command>` / `/disable <command>` — toggle a command; run without an argument to list them

### Owner commands

Available only to `CREATOR_ID`:

- `/addadmin <id> <username>` — add an admin, e.g. `/addadmin 123456789 username`
- `/addchat` — add a chat to the allowlist; run it in the chat or pass a chat ID
- `/chatmodel <model>` — set the OpenAI model for chat and vision
- `/imgmodel <model>` — set the model for image generation
- `/slowmode` — configure the rate limit (default: 10 requests per hour per user)
