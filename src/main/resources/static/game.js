const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');
const HEX_SIZE = 20;
const BOARD_SIZE = 11;
let currentPlayer = 1;

function drawHexagon(x, y, color) {
    ctx.beginPath();
    for (let i = 0; i < 6; i++) {
        const angle = (Math.PI / 3) * i;
        const hx = x + HEX_SIZE * Math.cos(angle);
        const hy = y + HEX_SIZE * Math.sin(angle);
        ctx.lineTo(hx, hy);
    }
    ctx.closePath();
    ctx.fillStyle = color;
    ctx.fill();
    ctx.stroke();
}

function drawBoard(board) {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    const offsetX = canvas.width / 2 - (BOARD_SIZE * HEX_SIZE * 1.5) / 2;
    const offsetY = canvas.height / 2 - (BOARD_SIZE * HEX_SIZE * Math.sqrt(3)) / 2;

    for (let row = 0; row < BOARD_SIZE; row++) {
        for (let col = 0; col < BOARD_SIZE; col++) {
            const x = offsetX + col * HEX_SIZE * 1.5 + (row % 2) * HEX_SIZE * 0.75;
            const y = offsetY + row * HEX_SIZE * Math.sqrt(3);
            const color = board[row][col] === 0 ? '#ffffff' : board[row][col] === 1 ? '#ff0000' : '#0000ff';
            drawHexagon(x, y, color);
        }
    }
}

function getHexFromMouse(x, y) {
    const offsetX = canvas.width / 2 - (BOARD_SIZE * HEX_SIZE * 1.5) / 2;
    const offsetY = canvas.height / 2 - (BOARD_SIZE * HEX_SIZE * Math.sqrt(3)) / 2;
    x -= offsetX;
    y -= offsetY;
    const row = Math.floor(y / (HEX_SIZE * Math.sqrt(3)));
    const col = Math.floor((x - (row % 2) * HEX_SIZE * 0.75) / (HEX_SIZE * 1.5));
    if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
        return { row, col };
    }
    return null;
}

async function fetchBoard() {
    const response = await fetch('/api/board');
    const board = await response.json();
    drawBoard(board);
}

async function makeMove(row, col) {
    const response = await fetch('/api/move', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ row, col, player: currentPlayer })
    });
    const result = await response.json();
    if (result.success) {
        await fetchBoard();
        if (result.winner) {
            document.getElementById('status').textContent = `Người chơi ${result.winner} thắng!`;
            canvas.removeEventListener('click', handleClick);
        } else {
            currentPlayer = 3 - currentPlayer; // Chuyển lượt
            document.getElementById('status').textContent = `Lượt của người chơi ${currentPlayer} (${currentPlayer === 1 ? 'Đỏ' : 'Xanh'})`;
        }
    }
}

async function resetGame() {
    await fetch('/api/reset');
    currentPlayer = 1;
    document.getElementById('status').textContent = 'Lượt của người chơi 1 (Đỏ)';
    await fetchBoard();
    canvas.addEventListener('click', handleClick);
}

function handleClick(event) {
    const rect = canvas.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;
    const hex = getHexFromMouse(x, y);

    if (!hex) return; // Kiểm tra xem có chọn đúng ô trên bàn cờ không
    makeMove(hex.row, hex.col);
}
canvas.addEventListener('click', handleClick);
fetchBoard(); // Vẽ bàn cờ ban đầu