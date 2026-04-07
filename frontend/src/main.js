import "./style.css";

const $ = (id) => document.getElementById(id);

const state = {
  apiBase: "",
  gameId: null,
  playerId: null,
  lastGame: null,
};

function apiUrl(path) {
  const base = state.apiBase.replace(/\/$/, "");
  return base + path;
}

function showError(msg) {
  const el = $("errorLine");
  const info = $("infoLine");
  el.textContent = msg || "";
  el.hidden = !msg;
  if (msg) info.hidden = true;
}

function showInfo(msg) {
  const el = $("infoLine");
  el.textContent = msg || "";
  el.hidden = !msg;
  if (msg) $("errorLine").hidden = true;
}

async function parseError(res) {
  const text = await res.text();
  try {
    const j = JSON.parse(text);
    return j.message || j.error || text || res.statusText;
  } catch {
    return text || res.statusText || String(res.status);
  }
}

async function fetchJson(url, options = {}) {
  const res = await fetch(url, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {}),
    },
  });
  if (!res.ok) {
    throw new Error(`${res.status}: ${await parseError(res)}`);
  }
  const ct = res.headers.get("content-type") || "";
  if (ct.includes("application/json")) {
    return res.json();
  }
  return null;
}

function faceLabel(face) {
  if (face === 6) return "STAR";
  return String(face);
}

function formatDice(arr) {
  if (!arr || !arr.length) return "—";
  return arr.map((f) => (f === 6 ? "★" : String(f))).join(", ");
}

function renderPlayers(game) {
  const box = $("playersBox");
  const rows = (game.players || [])
    .map(
      (p) =>
        `<tr class="${p.eliminated ? "elim" : ""}"><td>${escapeHtml(p.name)}${
          p.cpu ? " (CPU)" : ""
        }</td><td>${p.diceCount}</td><td>${p.eliminated ? "脱落" : "生存"}</td>${
          p.id === state.playerId ? "<td>あなた</td>" : "<td></td>"
        }</tr>`,
    )
    .join("");
  box.innerHTML =
    "<table><thead><tr><th>名前</th><th>ダイス数</th><th>状態</th><th></th></tr></thead><tbody>" +
    rows +
    "</tbody></table>";
}

function escapeHtml(s) {
  return String(s)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function challengeResultLabel(code) {
  if (code === "BIDDER_LOSES") return "宣言者負け";
  if (code === "CHALLENGER_LOSES") return "挑戦者負け";
  if (code === "EXACT_MATCH") return "丁度一致";
  return code || "—";
}

function renderActionLog(game) {
  const box = $("actionLogBox");
  const content = $("actionLogContent");
  const log = game.actionLog;
  if (!log || !log.length) {
    box.hidden = true;
    content.innerHTML = "";
    return;
  }
  box.hidden = false;

  let html = "";
  let lastRound = null;
  for (const e of log) {
    if (e.type === "ROUND_START") {
      lastRound = e.round;
      html += `<div class="action-log-round">ラウンド ${e.round} 開始（先攻: ${escapeHtml(e.playerName)}）</div>`;
      continue;
    }
    if (lastRound !== e.round && e.round != null) {
      lastRound = e.round;
      html += `<div class="action-log-round">ラウンド ${e.round}</div>`;
    }
    if (e.type === "BID") {
      html += `<div class="action-log-entry bid">${escapeHtml(e.playerName)}: ${e.quantity}個の${escapeHtml(
        faceLabel(e.face),
      )}を宣言</div>`;
    } else if (e.type === "CHALLENGE") {
      const resJa = challengeResultLabel(e.challengeResult);
      html += `<div class="action-log-entry challenge">${escapeHtml(e.playerName)}: チャレンジ！（実際 ${
        e.actualCount
      }個 → ${escapeHtml(resJa)}）`;
      if (e.penaltyDescription) {
        html += `<div class="action-log-penalty">${escapeHtml(e.penaltyDescription)}</div>`;
      }
      html += `</div>`;
    }
  }
  content.innerHTML = html;
}

function refreshUI(game) {
  state.lastGame = game;
  $("playPanel").hidden = false;
  $("statusLine").textContent =
    `state: ${game.state}` +
    (game.currentPlayer ? ` / 現在の手番: ${game.currentPlayer}` : "");

  const win = $("winnerLine");
  if (game.state === "FINISHED") {
    win.hidden = false;
    if (game.winnerPlayerId === state.playerId) {
      win.textContent = "結果: あなたの勝ち";
    } else if (game.winnerPlayerId) {
      win.textContent = "結果: あなたの負け（相手の勝ち）";
    } else {
      win.textContent = "結果: FINISHED（勝者IDなし）";
    }
  } else {
    win.hidden = true;
  }

  renderPlayers(game);

  $("myDiceLine").textContent =
    game.myDice != null
      ? `あなたのダイス: ${formatDice(game.myDice)}`
      : "あなたのダイス: （viewerPlayerId なしで取得したため非表示）";

  if (game.currentBid) {
    $("bidLine").textContent =
      `直近の Bid: ${game.currentBid.quantity} 個の ${faceLabel(
        game.currentBid.face,
      )}（宣言者 ${game.currentBid.playerId}）`;
  } else {
    $("bidLine").textContent = "直近の Bid: なし（ラウンド初手）";
  }

  const isMyTurn =
    game.state === "PLAYING" &&
    game.currentPlayer &&
    game.currentPlayer === state.playerId;
  const canChallenge = isMyTurn && game.currentBid != null;
  $("actionBox").hidden = !isMyTurn;
  $("bidBtn").disabled = !isMyTurn;
  $("challengeBtn").disabled = !canChallenge;

  renderActionLog(game);
}

async function loadGame() {
  showError("");
  if (!state.gameId || !state.playerId) return;
  const q = new URLSearchParams({ viewerPlayerId: state.playerId });
  try {
    const game = await fetchJson(apiUrl(`/games/${state.gameId}?${q}`));
    showInfo("");
    refreshUI(game);
  } catch (e) {
    if (String(e.message).startsWith("404:")) {
      $("actionBox").hidden = true;
      const logBox = $("actionLogBox");
      if (logBox) logBox.hidden = true;
      showError("");
      showInfo(
        "このゲームはサーバー上に見つかりません（ID の誤りか、サーバー再起動で消えた可能性があります）。新しく遊ぶ場合はページを再読み込みしてゲームを作り直してください。",
      );
    } else {
      throw e;
    }
  }
}

async function onCreate() {
  showError("");
  state.apiBase = $("apiBase").value.trim();
  const name = $("playerName").value.trim() || "player";
  const cpuCount = parseInt($("cpuCount").value, 10);
  const body = { name, cpuCount };
  const created = await fetchJson(apiUrl("/games"), {
    method: "POST",
    body: JSON.stringify(body),
  });
  state.gameId = created.gameId;
  state.playerId = created.playerId;
  $("gameIdOut").textContent = state.gameId;
  $("playerIdOut").textContent = state.playerId;
  $("lobbyPanel").hidden = false;
  $("setupPanel").hidden = true;
}

async function onStart() {
  showError("");
  state.apiBase = $("apiBase").value.trim();
  await fetchJson(apiUrl(`/games/${state.gameId}/start`), {
    method: "POST",
    body: JSON.stringify({ playerId: state.playerId }),
  });
  $("lobbyPanel").hidden = true;
  await loadGame();
}

async function postAction(payload) {
  showError("");
  state.apiBase = $("apiBase").value.trim();
  await fetchJson(apiUrl(`/games/${state.gameId}/action`), {
    method: "POST",
    body: JSON.stringify(payload),
  });
  await loadGame();
}

function onBid() {
  const quantity = parseInt($("bidQty").value, 10);
  const face = parseInt($("bidFace").value, 10);
  if (!Number.isFinite(quantity) || quantity < 1) {
    showError("個数は 1 以上の整数にしてください");
    return;
  }
  if (!Number.isFinite(face) || face < 1 || face > 6) {
    showError("面は 1〜6（6=STAR）にしてください");
    return;
  }
  postAction({
    type: "BID",
    playerId: state.playerId,
    quantity,
    face,
  }).catch((e) => showError(e.message));
}

function onChallenge() {
  postAction({
    type: "CHALLENGE",
    playerId: state.playerId,
  }).catch((e) => showError(e.message));
}

function fillCpuSelect() {
  const sel = $("cpuCount");
  for (let i = 1; i <= 5; i++) {
    const o = document.createElement("option");
    o.value = String(i);
    o.textContent = String(i);
    sel.appendChild(o);
  }
  sel.value = "1";
}

$("createBtn").addEventListener("click", () =>
  onCreate().catch((e) => showError(e.message)),
);
$("startBtn").addEventListener("click", () =>
  onStart().catch((e) => showError(e.message)),
);
$("refreshBtn").addEventListener("click", () =>
  loadGame().catch((e) => showError(e.message)),
);
$("bidBtn").addEventListener("click", onBid);
$("challengeBtn").addEventListener("click", onChallenge);

fillCpuSelect();
