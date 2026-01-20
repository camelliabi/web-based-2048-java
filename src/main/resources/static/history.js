window.addEventListener("load", async () => {
  const res = await fetch("/api/history");
  const list = await res.json();

  const box = document.getElementById("history-list");
  box.innerHTML = "";

  for (const row of list) {
    const gameId = row.game_id;
    const record = row.record;

    const item = document.createElement("div");
    item.className = "history-item";
    item.innerHTML = `
      <button class="button">
        <a href="index.html?game_id=${encodeURIComponent(gameId)}">
          Game ${gameId}, Record: ${record}
        </a>
      </button>
    `;
    box.appendChild(item);
  }
});
