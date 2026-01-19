const SIZE = 4;
const board = document.getElementById("board");
let grid = empty();
let activeGameId = null;

//log to show active game id
console.log("Active Game ID:", activeGameId);

//load game on window load
window.addEventListener("load", () => {
  const gameId = new URLSearchParams(window.location.search).get("game_id");
  if (gameId) {
	activeGameId = Number(gameId);
    loadPreviousGame(1, activeGameId);
  } else {
	activeGameId = null;
	//load the most recent active game for user 1
    loadGame(1);    
	loadGameID(1);
  }
});

//new game button
document.getElementById("new-game-button").addEventListener("click", () => {
	newGame();
});

//game history button
document.getElementById("game-history-button").addEventListener("click", () => {
  const url = activeGameId ? `history.php?game_id=${encodeURIComponent(activeGameId)}` : 'history.html';
  window.location.href = url;
  //saveAllGames();
});

//start a new game
async function newGame() {
	//save current game before starting new one
	await saveAllGames();
	activeGameId = null; 
	grid = empty();
	addTile();
	addTile();
	drawAll();

	await saveProgress();
	history.replaceState(null, "", "index.php");
}

//save current progress
function saveProgress(){
	return fetch('save_game.php', {
		method: 'POST',
		body: JSON.stringify({
			user_id: 1,
			game_id: activeGameId,
			data_json: grid}),
		headers: {
			'Content-Type': 'application/json'
		}
	});
}

//save all games for user
function saveAllGames(){
	return fetch('save_allgames.php', {
		method: 'POST',
		body: JSON.stringify({
			user_id: 1,
			game_id: activeGameId,
			data_json: grid}),
		headers: {
			'Content-Type': 'application/json'
		}
	});
}

//load game for user
function loadGame(user_id) {
	fetch('load_game.php?user_id=' + user_id)
		.then(response => response.json())
		.then(data => {
			const newGrid = [];

			for (let r = 0; r < data.data_json.length; r++) {
				const newRow = [];
				for (let c = 0; c < data.data_json[r].length; c++) {
					newRow.push(Number(data.data_json[r][c]));
				}
				newGrid.push(newRow);
			}

			grid = newGrid;
			drawAll();
		});
}

//load active game id for user
function loadGameID(user_id) {
	fetch('load_game_id.php?user_id=' + user_id)
		.then(response => response.json())
		.then(data => {
			activeGameId = Number(data.game_id);
			//history.replaceState(null, "", "index.php?game_id=" + activeGameId);
		});
	//console log to show active game id after load
    console.log("Active Game ID after load:", activeGameId);
}

//load previous game by id
function loadPreviousGame(user_id, game_id) {
	return fetch('load_previous_game.php?user_id=' + user_id + '&game_id=' + game_id)
		.then(response => response.json())
		.then(data => {
			const newGrid = [];
			
			for (let r = 0; r < data.data_json.length; r++) {
				const newRow = [];
				for (let c = 0; c < data.data_json[r].length; c++) {
					newRow.push(Number(data.data_json[r][c]));
				}
				newGrid.push(newRow);
			}
			grid = newGrid;

			drawAll();
			return saveAllGames();
		}).catch(error => {
			console.error('Error loading previous game:', error);
		});
}

//create empty grid
function empty() {	
	const arr = [];
	for (let r = 0; r < SIZE; r++) {
		for (let c = 0; c < SIZE; c++) {
			if (!arr[r]) arr[r] = [];
			arr[r][c] = 0;
		}
	}
	return arr;
}

//draw the grid
function drawAll() {
	//clear board
    board.innerHTML = "";
    for (let r = 0; r < SIZE; r++) {
		for (let c = 0; c < SIZE; c++) {
			const v = grid[r][c];
			const d = document.createElement("div");
			d.className = "cell";
			d.dataset.v = String(v);
			d.textContent = v === 0 ? "" : v;
			board.appendChild(d);
		}
    }
}

//get list of empty cells
function empties() {
	const cells = [];
    for (let r = 0; r < SIZE; r++) {
        for (let c = 0; c < SIZE; c++) {
          if (grid[r][c] === 0) cells.push([r,c]);
		}
	}
      return cells;
}

//add a new tile (2 or 4) to a random empty cell
function addTile() {
    const e = empties();
    if (e.length === 0) return;
	//choose random empty cell
    const index = Math.floor(Math.random() * e.length);
	//get row and column
	const pos = e[index];
	const r = pos[0];
	const c = pos[1];
	//90% chance of 2, 10% chance of 4
    grid[r][c] = Math.random() < 0.9 ? 2 : 4;
}

//make a copy of the grid
function gridCopy(g) { 
	const newGrid = []; 
	for (let r = 0; r < SIZE; r++) {
		for (let c = 0; c < SIZE; c++) {
			if (!newGrid[r]) newGrid[r] = [];
			newGrid[r][c] = g[r][c];
		}
	}
	return newGrid;
}

//check if two grids are the same
function same(a,b) {
    for (let r=0;r<SIZE;r++) { 
		for (let c=0;c<SIZE;c++) {
			if (a[r][c] !== b[r][c]) return false;
		}
	}
    return true;
}

// merge one line to the left
function mergeLine(line) {
	//remove zeros
	const newLine = line.filter(v => v !== 0);
	for (let i = 0; i < newLine.length - 1; i++) {
		if (newLine[i] === newLine[i+1]) {
			//merge tiles
			newLine[i] *= 2;
			//remove merged tile
			newLine.splice(i+1, 1);
		}
	}
	//add zeros to the end
	while (newLine.length < SIZE) newLine.push(0);
	return newLine;
}

function moveLeft() {
    const before = gridCopy(grid);
    for (let r = 0; r < SIZE; r++) {
		grid[r] = mergeLine(grid[r]);
    }
    return !same(before, grid);
}

//move right by reversing, merging left, then reversing again
function moveRight() {
    const before = gridCopy(grid);
    for (let r = 0; r < SIZE; r++) {
		grid[r] = mergeLine(grid[r].slice().reverse()).reverse();
	}
    return !same(before, grid);
}

//move up by merging columns
function moveUp() {
    const before = gridCopy(grid);
    for (let c = 0; c < SIZE; c++) {
		const col = [];
		//extract the column
        for (let r = 0; r < SIZE; r++) {
			col.push(grid[r][c]);
        }
		//merge the column
        const merged = mergeLine(col);
		//put back the merged column
        for (let r = 0; r < SIZE; r++) {
			grid[r][c] = merged[r];
        }
    }
    return !same(before, grid);
}

function moveDown() {
    const before = gridCopy(grid);
    for (let c = 0; c < SIZE; c++) {
		const col = [];
		//extract the column
        for (let r = 0; r < SIZE; r++) {
			col.push(grid[r][c]);
        }
		//merge the column and reverse
        const merged = mergeLine(col.slice().reverse()).reverse();
		//put back the merged column
        for (let r = 0; r < SIZE; r++) {
			grid[r][c] = merged[r];
        }
    }
    return !same(before, grid);
}

function step(dir) {
    let moved = false;
    if (dir === "L") moved = moveLeft();
    if (dir === "R") moved = moveRight();
    if (dir === "U") moved = moveUp();
    if (dir === "D") moved = moveDown();

    if (moved) {
		addTile();
		saveProgress();
		if (activeGameId) saveAllGames();
	}
	isGameOver();
	console.log("Active Game ID after move:", activeGameId);
	drawAll();
	
}

//check if any moves are possible
function canMove() {
	for (let r = 0; r < SIZE; r++) {
		for (let c = 0; c < SIZE; c++) {
			if (grid[r][c] === 0) return true;
			if (c < SIZE - 1 && grid[r][c] === grid[r][c+1]) return true;
			if (r < SIZE - 1 && grid[r][c] === grid[r+1][c]) return true;
		}
	}
	return false;
}

function isGameOver() {
	if (!canMove()) {
		alert("Oops! Game is Over. :(");
	}
}

//keyboard controls	
document.addEventListener("keydown", (e) => {
    const k = e.key;
    if (["ArrowLeft","ArrowRight","ArrowUp","ArrowDown"].includes(k)) e.preventDefault();
    if (k === "ArrowLeft") step("L");
    if (k === "ArrowRight") step("R");
    if (k === "ArrowUp") step("U");
    if (k === "ArrowDown") step("D");
});

//move tiles while dragging mouse
let startX = null;
let startY = null;
document.addEventListener("mousedown", (e) => {
	startX = e.clientX;
	startY = e.clientY;
});

document.addEventListener("mouseup", (e) => {
	if (startX === null || startY === null) return;
	const dx = e.clientX - startX;
	const dy = e.clientY - startY;	
	if (Math.abs(dx) > Math.abs(dy)) {
		if (dx > 30) step("R");
		else if (dx < -30) step("L");
	} else {
		if (dy > 30) step("D");
		else if (dy < -30) step("U");
	}
	startX = null;
	startY = null;
});	

//move tiles while touching screen
let touchStartX = null;
let touchStartY = null;

document.addEventListener("touchstart", (e) => {
	touchStartX = e.touches[0].clientX;
	touchStartY = e.touches[0].clientY;
});

document.addEventListener("touchend", (e) => {
	if (touchStartX === null || touchStartY === null) return;
	const dx = e.changedTouches[0].clientX - touchStartX;
	const dy = e.changedTouches[0].clientY - touchStartY;	
	if (Math.abs(dx) > Math.abs(dy)) {
		if (dx > 30) step("R");
		else if (dx < -30) step("L");
	} else {
		if (dy > 30) step("D");
		else if (dy < -30) step("U");
	}
	touchStartX = null;
	touchStartY = null;
});	



