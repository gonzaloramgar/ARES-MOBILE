const express = require('express');
const path = require('path');
const fs = require('fs');

const app = express();
const PORT = 3000;
const DATA_DIR = path.join(__dirname, 'data');
const TAB_CONFIG_FILE = path.join(DATA_DIR, 'tab-design-config.json');

const defaultTabConfig = {
  chat: 'vibrant',
  memory: 'classic',
  tasks: 'subtle',
  settings: 'minimal'
};

function ensureTabConfigFile() {
  if (!fs.existsSync(DATA_DIR)) {
    fs.mkdirSync(DATA_DIR, { recursive: true });
  }

  if (!fs.existsSync(TAB_CONFIG_FILE)) {
    fs.writeFileSync(TAB_CONFIG_FILE, JSON.stringify(defaultTabConfig, null, 2));
  }
}

function readTabConfig() {
  try {
    ensureTabConfigFile();
    const raw = fs.readFileSync(TAB_CONFIG_FILE, 'utf-8');
    const parsed = JSON.parse(raw);
    return { ...defaultTabConfig, ...parsed };
  } catch (error) {
    return { ...defaultTabConfig };
  }
}

function writeTabConfig(config) {
  ensureTabConfigFile();
  fs.writeFileSync(TAB_CONFIG_FILE, JSON.stringify(config, null, 2));
}

app.use(express.static(path.join(__dirname, 'public')));
app.use(express.json());

// API endpoint para obtener sugerencias de diseño
// API endpoints
app.get('/api/design-variants', (req, res) => {
  res.json({
    variants: [
      {
        id: 'chat-minimal',
        name: 'Chat Minimal',
        description: 'Diseño minimalista con campo y botón',
        preview: 'minimal',
        complexity: 'low',
        recommended: false
      },
      {
        id: 'chat-chatgpt',
        name: 'Chat ChatGPT Style',
        description: 'Barra similar a ChatGPT con placeholder amigable',
        preview: 'chatgpt',
        complexity: 'medium',
        recommended: false
      },
      {
        id: 'chat-glassmorphism',
        name: 'Chat Glassmorphism',
        description: 'Efecto vidrio frosted con blur',
        preview: 'glass',
        complexity: 'high',
        recommended: false
      },
      {
        id: 'chat-neon',
        name: 'Chat Neon ARES',
        description: 'Estilo neon rojo característico de ARES',
        preview: 'neon',
        complexity: 'medium',
        recommended: true
      }
    ]
  });
});

app.get('/api/implementation/:variant', (req, res) => {
  const { variant } = req.params;
  const implementations = {
    minimal: {
      file: 'ChatScreen.kt',
      location: 'Line ~159',
      complexity: 'Low - Already implemented',
      code: 'Row(horizontalArrangement = Arrangement.spacedBy(8.dp))'
    },
    chatgpt: {
      file: 'ChatScreen.kt',
      location: 'Line ~159',
      complexity: 'Medium - Change border-radius to 28.dp',
      code: 'Surface(shape = RoundedCornerShape(28.dp), border = BorderStroke(1.dp, Color(0x4d4d4d)))'
    },
    glass: {
      file: 'ChatScreen.kt',
      location: 'Line ~159',
      complexity: 'High - Requires Compose 1.4+, uses Modifier.blur()',
      code: 'modifier = Modifier.blur(radius = 16.dp)'
    },
    neon: {
      file: 'ChatScreen.kt',
      location: 'Line ~159',
      complexity: 'Medium - Uses existing ARES colors',
      code: 'border = BorderStroke(2.dp, Color(0xff2020)), shadow with spotColor'
    }
  };

  res.json(implementations[variant] || { error: 'Variant not found' });
});

app.get('/api/tab-design-config', (req, res) => {
  res.json(readTabConfig());
});

app.post('/api/tab-design-config', (req, res) => {
  const validVariants = new Set(['vibrant', 'classic', 'subtle', 'minimal', 'cyberpunk', 'metallic']);
  const safeConfig = {
    ...defaultTabConfig,
    ...(req.body || {})
  };

  const tabKeys = Object.keys(defaultTabConfig);
  for (const key of tabKeys) {
    if (!validVariants.has(safeConfig[key])) {
      return res.status(400).json({ error: `Invalid variant for ${key}` });
    }
  }

  writeTabConfig(safeConfig);
  return res.json({ ok: true, config: safeConfig });
});

// Routes
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.get('/compare', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'compare.html'));
});

app.get('/neon', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'neon-variants.html'));
});

app.get('/tabs-designer', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'tabs-designer.html'));
});

app.listen(PORT, () => {
  console.log(`
╔════════════════════════════════════════╗
║   ARES Design Preview Server           ║
║   http://localhost:${PORT}               ║
║   Abre tu navegador para ver mockups    ║
╚════════════════════════════════════════╝
  `);
});
