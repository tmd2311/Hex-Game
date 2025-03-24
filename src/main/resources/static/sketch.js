let hexSize = 20; // Kích thước của mỗi lục giác
let cols = 11; // Số cột (mỗi hàng có 11 ô)
let rows = 11; // Số hàng
let dx, dy; // Khoảng cách giữa các lục giác
let board = []; // Mảng 2D để lưu trạng thái của bảng (0: trống, 1: đỏ, 2: xanh dương)
let currentPlayer = 1; // 1: đỏ, 2: xanh dương
let offsetX = 50; // Dịch chuyển để căn giữa lưới
let offsetY = 50;

function setup() {
    createCanvas(600, 500); // Tạo canvas
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

    // Vẽ viền đỏ cho cạnh trái và phải
    stroke(200, 0, 0); // Màu đỏ
    strokeWeight(5); // Độ dày viền
    noFill();
    drawBorder(0, true); // Viền đỏ cho cạnh trái và phải

    // Vẽ viền xanh dương cho cạnh trên và dưới
    stroke(0, 0, 200); // Màu xanh dương
    strokeWeight(5);
    noFill();
    drawBorder(0, false); // Viền xanh dương cho cạnh trên và dưới
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
// Hàm vẽ viền cho các cạnh cụ thể của các ô ở biên
function drawBorder(offset, isRed) {
    if (isRed) {
        stroke(0, 0, 200); // Màu đỏ
        strokeWeight(3);
        for (let col = 0; col < cols; col++) {
            let x = col * dx; // Tâm của ô đầu cột
            let y = 0;

            // Cạnh 1: từ đỉnh 0 đến đỉnh 1
            let angle1 = radians(60*2  + 90);
            let angle2 = radians(60 * 3 + 90);
            let x1 = x + (hexSize + offset) * cos(angle1);
            let y1 = y + (hexSize + offset) * sin(angle1);
            let x2 = x + (hexSize + offset) * cos(angle2);
            let y2 = y + (hexSize + offset) * sin(angle2);
            line(x1, y1, x2, y2);

            // Cạnh 2: từ đỉnh 1 đến đỉnh 2
            let angle3 = radians(60 * 4 + 90);
            let x3 = x + (hexSize + offset) * cos(angle3);
            let y3 = y + (hexSize + offset) * sin(angle3);
            line(x2, y2, x3, y3);
        }

        // Cạnh 5, 6 của ô cuối cột (row = rows - 1)
        for (let col = 0; col < cols; col++) {
            let x = col * dx + (rows - 1) * (dx / 2); // Tâm của ô cuối cột
            let y = (rows - 1) * dy;

            // Cạnh 5: từ đỉnh 4 đến đỉnh 5
            let angle4 = radians(60 * 5 + 90);
            let angle5 = radians(60 * 6 + 90);
            let x4 = x + (hexSize + offset) * cos(angle4);
            let y4 = y + (hexSize + offset) * sin(angle4);
            let x5 = x + (hexSize + offset) * cos(angle5);
            let y5 = y + (hexSize + offset) * sin(angle5);
            line(x4, y4, x5, y5);

            // Cạnh 6: từ đỉnh 5 đến đỉnh 0
            let angle0 = radians(60 * 1 + 90);
            let x0 = x + (hexSize + offset) * cos(angle0);
            let y0 = y + (hexSize + offset) * sin(angle0);
            line(x5, y5, x0, y0);
        }
    } else {
        // Vẽ viền xanh dương: cạnh 1, 6 của ô đầu hàng (col = 0) và cạnh 3, 4 của ô cuối hàng (col = cols - 1)
        stroke(200, 0, 0); // Màu xanh dương
        strokeWeight(3);

        // Cạnh 1, 6 của ô đầu hàng (col = 0)
        for (let row = 0; row < rows; row++) {
            let x = row * (dx / 2); // Tâm của ô đầu hàng
            let y = row * dy;

            // Cạnh 1: từ đỉnh 0 đến đỉnh 1
            let angle0 = radians(60 * 1 + 90);
            let angle1 = radians(60 * 2 + 90);
            let x0 = x + (hexSize + offset) * cos(angle0);
            let y0 = y + (hexSize + offset) * sin(angle0);
            let x1 = x + (hexSize + offset) * cos(angle1);
            let y1 = y + (hexSize + offset) * sin(angle1);
            line(x0, y0, x1, y1);

            // Cạnh 6: từ đỉnh 5 đến đỉnh 0
            let angle5 = radians(60 * 6 + 90);
            let x5 = x + (hexSize + offset) * cos(angle5);
            let y5 = y + (hexSize + offset) * sin(angle5);
            line(x5, y5, x0, y0);
        }

        // Cạnh 3, 4 của ô cuối hàng (col = cols - 1)
        for (let row = 0; row < rows; row++) {
            let x = (cols - 1) * dx + row * (dx / 2); // Tâm của ô cuối hàng
            let y = row * dy;

            // Cạnh 3: từ đỉnh 2 đến đỉnh 3
            let angle2 = radians(60 * 3 + 90);
            let angle3 = radians(60 * 4 + 90);
            let x2 = x + (hexSize + offset) * cos(angle2);
            let y2 = y + (hexSize + offset) * sin(angle2);
            let x3 = x + (hexSize + offset) * cos(angle3);
            let y3 = y + (hexSize + offset) * sin(angle3);
            line(x2, y2, x3, y3);

            // Cạnh 4: từ đỉnh 3 đến đỉnh 4
            let angle4 = radians(60 * 5 + 90);
            let x4 = x + (hexSize + offset) * cos(angle4);
            let y4 = y + (hexSize + offset) * sin(angle4);
            line(x3, y3, x4, y4);
        }
    }
}

// Hàm xử lý khi người chơi nhấp chuột
function mousePressed() {
    let mx = mouseX - offsetX;
    let my = mouseY - offsetY;

    for (let row = 0; row < rows; row++) {
        for (let col = 0; col < cols; col++) {
            let x = col * dx + row * (dx / 2);
            let y = row * dy;
            let d = dist(mx, my, x, y);

            if (d < hexSize) { // Kiểm tra người chơi nhấp vào một ô
                if (board[row][col] === 0) { // Ô trống
                    // Gọi API move với vị trí và người chơi hiện tại
                    callMoveAPI(row, col, currentPlayer);
                }
            }
        }
    }
}

// Hàm cập nhật chỉ báo lượt
function updateTurnIndicator() {
    let indicator = document.getElementById("turn-indicator");
    indicator.innerHTML = `<span style="color: ${currentPlayer === 1 ? 'red' : 'blue'}">Player: ${currentPlayer}</span>`;
}
async function callResetAPI() {
    try {
        const response = await fetch('/api/reset', {
            method: 'GET'
        });

        if (response.ok) {
            // Làm mới bảng lưới Hex trong giao diện
            resetBoardUI();
            const swapButton = document.getElementById("swap-button");
            swapButton.disabled = false; // Kích hoạt lại nút
            swapButton.style.opacity = "1"; // Trả lại trạng thái bình thường
        } else {
            alert("Có lỗi xảy ra khi gọi hàm reset.");
        }
    } catch (error) {
        console.error('Lỗi:', error);
        alert("Không thể kết nối đến server.");
    }
}

// Hàm làm mới bảng lưới Hex trong giao diện
function resetBoardUI() {
    for (let row = 0; row < rows; row++) {
        for (let col = 0; col < cols; col++) {
            board[row][col] = 0; // Reset tất cả ô về trạng thái trống (0)
        }
    }
    currentPlayer = 1; // Đặt lại lượt chơi về người chơi 1
    updateTurnIndicator(); // Cập nhật chỉ báo lượt
    redraw(); // Vẽ lại lưới lục giác
}


async function callMoveAPI(row, col, player) {
    const moveRequest = {
        row: row,         // Dòng người chơi muốn di chuyển
        col: col,         // Cột người chơi muốn di chuyển
        player: player    // Người chơi hiện tại (1: đỏ, 2: xanh dương)
    };

    try {
        const response = await fetch('/api/move', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(moveRequest)
        });

        if (response.ok) {
            const moveResponse = await response.json();
            if (moveResponse.success) {
                board[row][col] = player; // Cập nhật trạng thái bảng
                currentPlayer = (player === 1) ? 2 : 1; // Chuyển lượt
                updateTurnIndicator(); // Cập nhật chỉ báo lượt
                redraw(); // Vẽ lại lưới
                if (moveResponse.winner !== 0) {
                    alert(`Người chơi ${moveResponse.winner} đã thắng!`);
                }
            } else {
                alert("Nước đi không hợp lệ. Hãy thử lại!");
            }
        } else {
            alert("Có lỗi xảy ra khi gọi API move.");
        }
    } catch (error) {
        console.error('Lỗi:', error);
        alert("Không thể kết nối đến server.");
    }
}

async function callSwapAPI() {
    try {
        const response = await fetch('/api/swap', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ swap: true })
        });

        if (response.ok) {
            const data = await response.json();
            if (data.success) {
                afterSwap(); // Vô hiệu hóa nút Swap sau khi thành công
            } else {
                alert("Swap không thành công: " + data.message);
            }
        } else {
            alert("Có lỗi xảy ra khi gọi API Swap.");
        }
    } catch (error) {
        console.error('Lỗi:', error);
        alert("Không thể kết nối đến server.");
    }
}

function afterSwap() {
    // Chuyển trạng thái người chơi: Người chơi 2 trở thành người chơi đầu tiên
    currentPlayer = 2;

    // Vô hiệu hóa nút Swap
    const swapButton = document.getElementById("swap-button");
    swapButton.disabled = true; // Vô hiệu hóa nút
    swapButton.style.opacity = "0.5"; // Làm mờ nút để hiển thị trạng thái không hoạt động

    // Cập nhật chỉ báo lượt chơi
    updateTurnIndicator();

    // Vẽ lại bảng Hex để phản ánh trạng thái mới
    resetBoardUI_afterswap();

    // Hiển thị thông báo swap
    alert(`Swap đã được thực hiện! Người chơi hiện tại: ${currentPlayer === 1 ? 'Đỏ' : 'Xanh dương'}`);
}
function resetBoardUI_afterswap() {
    for (let row = 0; row < rows; row++) {
        for (let col = 0; col < cols; col++) {
            board[row][col] = 0; // Reset tất cả ô về trạng thái trống (0)
        }
    }
    currentPlayer = 2; // Đặt lại lượt chơi về người chơi 1
    updateTurnIndicator(); // Cập nhật chỉ báo lượt
    redraw(); // Vẽ lại lưới lục giác
}