# Use a local Ollama model

Crystal Bot can keep using Gemini or send text-generation requests to an Ollama server that you run. Ollama is not bundled with the app, and the app does not install or start it for you.

## Set up Ollama

1. Install Ollama on your computer from [ollama.com](https://ollama.com/) and start it.
2. Download a model, for example:

   ```sh
   ollama pull llama3.2
   ```

3. Keep the Ollama server running while using Crystal Bot.

## Connect the Android app

Open **Settings**, choose **Ollama (local)**, enter the model name exactly as it appears in Ollama (for example `llama3.2`), and set the server URL:

- Android emulator: `http://10.0.2.2:11434` (the default; this reaches the development computer from the emulator).
- Physical Android device: use the computer's LAN IP, such as `http://192.168.1.20:11434`. The phone and computer must be on the same network.

Ollama normally listens on the computer's loopback interface. To allow a phone to reach it, configure the Ollama server to listen on `0.0.0.0:11434` before starting `ollama serve`. In PowerShell, for example:

```powershell
$env:OLLAMA_HOST = "0.0.0.0:11434"
ollama serve
```

Allow the port through the computer's firewall only on a trusted private network. Ollama's local API does not provide authentication by default: do not expose this port to the public internet or an untrusted network.

The provider, URL, and model are stored in the app's private settings and restored on restart. A Gemini key entered in Settings remains in memory for the current app session only; use the local secrets configuration if you want Gemini credentials available after a restart. A model must already be pulled into the Ollama server. The app does not silently fall back to Gemini or a canned response if local generation fails.

## What local mode uses

Chat, the agent collaboration text requests, and terminal text requests use the selected Ollama model and your computer's hardware. Ollama receives the conversation and any attached images; image attachments require a vision-capable model. Local mode does not provide live web search, and its DeepSearch mode does not add generated web citations. Image generation remains Gemini-only and is disabled while Ollama is selected. Switching back to Gemini sends supported requests through Gemini as before and remains subject to Google's applicable billing, quotas, and usage limits; choosing Ollama does not alter or bypass any provider charges or limits.
