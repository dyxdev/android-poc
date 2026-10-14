// Servidor local de tokens de reproducción de Mux (SOLO para la PoC, nunca en prod).
// Firma un JWT RS256 con sub = playback ID, aud = "v", kid y exp mayor que la
// duración del vídeo (25,5 s), y lo sirve como { playbackId, token }.
//
// Las claves las ejecuta Andy en SU máquina; este archivo nunca las lee de otro
// sitio que no sean variables de entorno del proceso:
//
//   node --env-file=<su .env.local> tools/token-server.mjs
//
// .env.local (NO en el repo, ver tools/.env.example):
//   MUX_SIGNING_KEY_ID=...
//   MUX_SIGNING_PRIVATE_KEY=-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----
//   MUX_SIGNED_PLAYBACK_ID=...
import { createSign, createPrivateKey } from "node:crypto";
import { createServer } from "node:http";

const PORT = 8787;
// exp = ahora + 10 min >> duración del vídeo (25,5 s): no caduca a mitad de reproducción.
const EXP_TTL_SECONDS = 600;

function base64url(input) {
  return Buffer.from(input).toString("base64url");
}

function required(name) {
  const value = process.env[name];
  if (!value) {
    console.error(`Falta la variable de entorno ${name}`);
    process.exit(1);
  }
  return value;
}

const keyId = required("MUX_SIGNING_KEY_ID");
const privateKeyPem = required("MUX_SIGNING_PRIVATE_KEY").replace(/\\n/g, "\n");
const playbackId = required("MUX_SIGNED_PLAYBACK_ID");

function signToken() {
  const now = Math.floor(Date.now() / 1000);
  const header = { alg: "RS256", typ: "JWT", kid: keyId };
  const payload = { sub: playbackId, aud: "v", exp: now + EXP_TTL_SECONDS, kid: keyId };
  const unsigned = `${base64url(JSON.stringify(header))}.${base64url(JSON.stringify(payload))}`;
  const key = createPrivateKey(privateKeyPem);
  const signature = createSign("RSA-SHA256").update(unsigned).sign(key, "base64url");
  return `${unsigned}.${signature}`;
}

createServer((req, res) => {
  if (req.method !== "GET" || req.url !== "/") {
    res.writeHead(404).end();
    return;
  }
  try {
    const token = signToken();
    res.writeHead(200, { "content-type": "application/json" });
    res.end(JSON.stringify({ playbackId, token }));
  } catch (err) {
    console.error("No se pudo firmar:", err.message);
    res.writeHead(500).end();
  }
}).listen(PORT, "127.0.0.1", () => console.log(`token-server en http://127.0.0.1:${PORT}`));
