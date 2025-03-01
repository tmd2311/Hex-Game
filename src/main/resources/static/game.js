const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');

const HEX_SIZE = 25; // Điều chỉnh kích thước hex
const BOARD_WIDTH = 11;
const BOARD_HEIGHT = 11;
const HEX_HEIGHT = Math.sqrt(3) * HEX_SIZE;
const HEX_WIDTH = 2 * HEX_SIZE;
const HORIZONTAL_SPACING = 1.5 * HEX_SIZE;
const VERTICAL_SPACING = HEX_HEIGHT;

// Khởi tạo bàn cờ
let board = Array.from({ length: BOARD_HEIGHT }, () => Array(BOARD_WIDTH).fill(0));
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
    ctx.strokeStyle = "#000";
    ctx.stroke();
}

function drawBoard() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    const offsetX = (canvas.width - BOARD_WIDTH * HORIZONTAL_SPACING) / 2;
    const offsetY = (canvas.height - BOARD_HEIGHT * VERTICAL_SPACING) / 2;

    for (let row = 0; row < BOARD_HEIGHT; row++) {
        for (let col = 0; col < BOARD_WIDTH; col++) {
            const x = offsetX + col * HORIZONTAL_SPACING;
            const y = offsetY + row * VERTICAL_SPACING + (col % 2) * (HEX_HEIGHT / 2);
            const color = board[row][col] === 0 ? "#fff" : board[row][col] === 1 ? "#ff0000" : "#0000ff";
            drawHexagon(x, y, color);
        }
    }
}

function getHexFromMouse(x, y) {
    const offsetX = (canvas.width - BOARD_WIDTH * HORIZONTAL_SPACING) / 2;
    const offsetY = (canvas.height - BOARD_HEIGHT * VERTICAL_SPACING) / 2;
    x -= offsetX;
    y -= offsetY;

    let col = Math.round(x / HORIZONTAL_SPACING);
    let row = Math.round((y - (col % 2) * (HEX_HEIGHT / 2)) / VERTICAL_SPACING);

    if (row >= 0 && row < BOARD_HEIGHT && col >= 0 && col < BOARD_WIDTH) {
        return { row, col };
    }
    return null;
}

function handleClick(event) {
    const rect = canvas.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;
    const hex = getHexFromMouse(x, y);

    if (!hex) return;
    if (board[hex.row][hex.col] === 0) {
        board[hex.row][hex.col] = currentPlayer;
        currentPlayer = 3 - currentPlayer;
        drawBoard();
    }
}

canvas.addEventListener("click", handleClick);
drawBoard();
