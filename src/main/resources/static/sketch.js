let hexSize = 20; // Kích thước của mỗi lục giác
let cols = 11; // Số cột (mỗi hàng có 11 ô)
let rows = 11; // Số hàng
let dx, dy; // Khoảng cách giữa các lục giác
let board = []; // Mảng 2D để lưu trạng thái của bảng (0: trống, 1: đỏ, 2: xanh dương)
let currentPlayer = 1; // 1: đỏ, 2: xanh dương
let offsetX = 50; // Dịch chuyển để căn giữa lưới
let offsetY = 50;

function setup() {
    createCanvas(500, 500); // Tạo canvas
    dx = hexSize * sqrt(3); // Khoảng cách ngang giữa các tâm lục giác
    dy = hexSize * 1.5; // Khoảng cách dọc giữa các tâm lục giác

    // Khởi tạo bảng
    for (let row = 0; row < rows; row++) {
        board[row] = [];
        for (let col = 0; col < cols; col++) {
            board[row][col] = 0; // 0 nghĩa là ô trống
        }
    }

    // Cập nhật chỉ báo lượt
    updateTurnIndicator();
}

function draw() {
    background(47, 47, 47); // Nền tối
    translate(offsetX, offsetY); // Dịch chuyển để căn giữa

    // Vẽ lưới lục giác
    drawHexGrid();

    // Vẽ viền đỏ
    stroke(200, 0, 0); // Màu đỏ
    strokeWeight(3); // Độ dày viền
    noFill();
    drawBorder(2); // Viền đỏ cách lưới 2 pixel

    // Vẽ viền xanh dương
    stroke(0, 0, 200); // Màu xanh dương
    strokeWeight(3);
    noFill();
    drawBorder(5); // Viền xanh dương cách lưới 5 pixel
}

// Hàm vẽ một lục giác tại vị trí (x, y)
function drawHexagon(x, y, fillColor) {
    beginShape();
    for (let i = 0; i < 6; i++) {
        let angle = radians(60 * i + 90); // Xoay 90 độ
        let px = x + hexSize * cos(angle);
        let py = y + hexSize * sin(angle);
        vertex(px, py);
    }
    endShape(CLOSE);

    if (fillColor) {
        fill(fillColor);
        noStroke();
        beginShape();
        for (let i = 0; i < 6; i++) {
            let angle = radians(60 * i + 90); // Xoay 90 độ
            let px = x + hexSize * 0.9 * cos(angle); // Thu nhỏ một chút để tạo viền
            let py = y + hexSize * 0.9 * sin(angle);
            vertex(px, py);
        }
        endShape(CLOSE);
    }
}

// Hàm vẽ lưới lục giác (mỗi hàng có 11 ô, hình kim cương)
function drawHexGrid() {
    for (let row = 0; row < rows; row++) {
        for (let col = 0; col < cols; col++) {
            // Tính toán vị trí x, y để tạo hình kim cương
            let x = col * dx + row * (dx / 2); // Dịch chuyển x theo hàng để tạo hình kim cương
            let y = row * dy;

            // Xác định màu tô dựa trên trạng thái ô
            let fillColor = null;
            if (board[row][col] === 1) {
                fillColor = color(255, 0, 0); // Đỏ
            } else if (board[row][col] === 2) {
                fillColor = color(0, 0, 255); // Xanh dương
            }

            // Vẽ lục giác
            stroke(100); // Viền xám cho lưới
            strokeWeight(1);
            fill(50); // Màu nền của ô trống
            drawHexagon(x, y, fillColor);
        }
    }
}

// Hàm vẽ đường viền bao quanh lưới
function drawBorder(offset) {
    let points = [];

    // Tính toán các điểm viền trên cùng
    for (let col = 0; col < cols; col++) {
        let x = col * dx - offset;
        let y = -hexSize - offset;
        points.push([x, y]);
    }

    // Tính toán các điểm viền bên phải
    for (let row = 0; row < rows; row++) {
        let x = (cols - 1) * dx + row * (dx / 2) + hexSize * sqrt(3) / 2 + offset;
        let y = row * dy;
        points.push([x, y]);
    }

    // Tính toán các điểm viền dưới cùng
    for (let col = cols - 1; col >= 0; col--) {
        let x = col * dx + (rows - 1) * (dx / 2) - offset;
        let y = (rows - 1) * dy + hexSize + offset;
        points.push([x, y]);
    }

    // Tính toán các điểm viền bên trái
    for (let row = rows - 1; row >= 0; row--) {
        let x = row * (dx / 2) - hexSize * sqrt(3) / 2 - offset;
        let y = row * dy;
        points.push([x, y]);
    }

    // Vẽ đường viền
    beginShape();
    for (let i = 0; i < points.length; i++) {
        vertex(points[i][0], points[i][1]);
    }
    endShape(CLOSE);
}

// Hàm xử lý khi người chơi nhấp chuột
function mousePressed() {
    let mx = mouseX - offsetX;
    let my = mouseY - offsetY;

    // Tìm ô được nhấp
    for (let row = 0; row < rows; row++) {
        for (let col = 0; col < cols; col++) {
            let x = col * dx + row * (dx / 2);
            let y = row * dy;
            let d = dist(mx, my, x, y);
            if (d < hexSize) {
                // Kiểm tra nếu ô trống
                if (board[row][col] === 0) {
                    board[row][col] = currentPlayer;
                    // Chuyển lượt
                    currentPlayer = currentPlayer === 1 ? 2 : 1;
                    updateTurnIndicator();
                }
            }
        }
    }
}

// Hàm cập nhật chỉ báo lượt
function updateTurnIndicator() {
    let indicator = document.getElementById("turn-indicator");
    indicator.innerHTML = `<span style="color: ${currentPlayer === 1 ? 'red' : 'blue'}">${currentPlayer}</span>`;
}