let hexSize = 20;
let cols = 11;
let rows = 11;
let dx = hexSize * Math.sqrt(3); // Khoảng cách ngang
let dy = hexSize * 1.5; // Khoảng cách dọc
let board = Array.from({ length: rows }, () => Array(cols).fill(0)); // Mảng 2D
let currentPlayer = 1;
let offsetX = 50;
let offsetY = 50;


function setup() {
    hexSize = 20; // Kích thước của lục giác
    rows = 11; // Số hàng của lưới
    cols = 11; // Số cột của lưới

    dx = hexSize * sqrt(3); // Khoảng cách ngang giữa các tâm lục giác
    dy = hexSize * 1.5; // Khoảng cách dọc giữa các tâm lục giác

    let gridWidth = cols * dx; // Chiều rộng của lưới lục giác
    let gridHeight = rows * dy; // Chiều cao của lưới lục giác

    // Tăng kích thước canvas để thêm khoảng trống bên phải
    let canvasWidth = gridWidth + 2 + dx; // Thêm khoảng dư để các ô không bị cắt
    let canvasHeight = gridHeight + 2 * dy; // Thêm khoảng dư trên và dưới

    createCanvas(canvasWidth + 200, canvasHeight + 50); // Tạo canvas lớn hơn

    // Điều chỉnh offset để căn giữa và dịch lưới xuống thấp hơn
    offsetX = (canvasWidth - gridWidth) / 2 + 35; // Giữ căn giữa ngang
    offsetY = (canvasHeight - gridHeight) / 2 + 35; // Dịch xuống thêm chút

    board = [];
    for (let row = 0; row < rows; row++) {
        board[row] = [];
        for (let col = 0; col < cols; col++) {
            board[row][col] = 0; // Khởi tạo trạng thái ô (trống)
        }
    }
    updateTurnIndicator();
}

function draw() {
    background(255,255,255); // Nền tối
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
            let fillColor = (255,255,255);
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

// Hàm cập nhật chỉ báo lượt
function updateTurnIndicator() {
    const indicator = document.getElementById("turn-indicator");
    if (!indicator) {
        console.error("Không tìm thấy phần tử 'turn-indicator'.");
        return;
    }
    indicator.innerHTML = `<span style="color: ${currentPlayer === 1 ? 'red' : 'blue'}">Player: ${currentPlayer}</span>`;
    console.log(`Indicator updated successfully: Player ${currentPlayer}`);
}
function callResetAPI(event) {
    if (event) {
        event.stopPropagation(); // Ngăn sự kiện lan rộng
    }

    // Logic reset
    fetch('/api/reset', {
        method: 'GET'
    }).then(response => {
        if (response.ok) {
            resetBoardUI(); // Làm mới giao diện
            const swapButton = document.getElementById("swap-button");
            swapButton.disabled = false;
            swapButton.style.opacity = "1";
        } else {
            alert("Có lỗi xảy ra khi gọi hàm reset.");
        }
    }).catch(error => {
        console.error("Lỗi:", error);
    });
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


function mousePressed(event) {
    // Ngăn sự kiện nếu nó đến từ các nút ngoài bảng
    if (event.target.tagName === "BUTTON") {
        console.log("Sự kiện chuột bị dừng vì đến từ nút.");
        return;
    }

    // Tính toán tọa độ chuột trên bảng
    let mx = mouseX - offsetX; // Lấy tọa độ chuột tương đối so với bảng Hex
    let my = mouseY - offsetY;

    let selectedRow = -1, selectedCol = -1;
    let minDistance = Infinity;

    // Duyệt qua từng ô lưới Hexagon để tìm ô gần nhất
    for (let row = 0; row < rows; row++) {
        for (let col = 0; col < cols; col++) {
            // Tính toán tọa độ trung tâm của mỗi lục giác
            let x = col * dx + row * (dx / 2);
            let y = row * dy;

            // Kiểm tra xem chuột có nằm trong lục giác không
            let d = dist(mx, my, x, y); // Khoảng cách giữa chuột và tâm lục giác
            if (d < hexSize && d < minDistance) {
                selectedRow = row;
                selectedCol = col;
                minDistance = d; // Cập nhật ô gần nhất
            }
        }
    }

    // Nếu tìm thấy ô lục giác hợp lệ
    if (selectedRow !== -1 && selectedCol !== -1) {
        console.log(`Ô lục giác được chọn: Row=${selectedRow}, Col=${selectedCol}`);
        callMoveAPI(selectedRow, selectedCol, currentPlayer); // Gọi API xử lý nước đi
    } else {
        console.log("Chuột nằm ngoài tất cả các lục giác."); // Log để debug
    }
}

function isPointInsideHexagon(mx, my, cx, cy, hexSize) {
    for (let angle = 0; angle < 360; angle += 60) {
        let ax = cx + hexSize * Math.cos(radians(angle));
        let ay = cy + hexSize * Math.sin(radians(angle));
        let bx = cx + hexSize * Math.cos(radians(angle + 60));
        let by = cy + hexSize * Math.sin(radians(angle + 60));

        let crossProduct = (mx - ax) * (by - ay) - (my - ay) * (bx - ax);
        if (crossProduct < 0) {
            return false; // Điểm nằm ngoài lục giác
        }
    }
    return true; // Điểm nằm trong lục giác
}

function radians(degrees) {
    return (degrees * Math.PI) / 180;
}

async function callMoveAPI(row, col, player) {
    const moveRequest = { row, col, player };

    try {
        const response = await fetch('/api/move', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(moveRequest) // Gửi tọa độ và người chơi hiện tại
        });

        if (response.ok) {
            const data = await response.json();
            if (data.success) {
                // Cập nhật trạng thái bảng trên client
                board[row][col] = player;
                currentPlayer = (player === 1) ? 2 : 1; // Chuyển lượt
                updateTurnIndicator();
                checkAndDisableSwap(); // Kiểm tra và vô hiệu hóa Swap nếu cần
                redraw(); // Vẽ lại giao diện

                // Kiểm tra người chiến thắng
                if (data.winner !== 0) {
                    console.log(`Player ${data.winner} thắng cuộc!`);
                    showWinDialog(data.winner); // Hiển thị thông báo chiến thắng
                }
            }
        } else {
            alert("Có lỗi xảy ra khi gọi API. Mã trạng thái: " + response.status);
        }
    } catch (error) {
        console.error("Lỗi kết nối:", error);
    }
}

function showWinDialog(winner) {
    // Tạo một phần tử div chứa thông báo chiến thắng
    let dialog = document.createElement("div");
    dialog.id = "winDialog";
    dialog.style.position = "fixed";
    dialog.style.top = "50%";
    dialog.style.left = "50%";
    dialog.style.transform = "translate(-50%, -50%)";
    dialog.style.background = "linear-gradient(to bottom right, #f5f5f5, #dcdcdc)"; // Gradient nhẹ nhàng, hiện đại
    dialog.style.borderRadius = "15px";
    dialog.style.padding = "40px";
    dialog.style.width = "400px";
    dialog.style.boxShadow = "0 10px 30px rgba(0, 0, 0, 0.2)"; // Hiệu ứng bóng nhẹ
    dialog.style.textAlign = "center";

    // Tiêu đề thông báo
    let title = document.createElement("h2");
    title.innerText = `NGƯỜI CHƠI SỐ ${winner} THẮNG! 🎉`;
    title.style.color = "#3b3b3b"; // Màu xám đậm hài hòa
    title.style.marginBottom = "25px";
    title.style.fontSize = "26px";
    title.style.fontWeight = "bold";
    dialog.appendChild(title);

    // Tạo container cho các nút để xếp theo hàng dọc
    let buttonContainer = document.createElement("div");
    buttonContainer.style.display = "flex";
    buttonContainer.style.flexDirection = "column";
    buttonContainer.style.gap = "20px";
    buttonContainer.style.alignItems = "center";

    // Nút NEW GAME
    let newGameButton = document.createElement("button");
    newGameButton.innerText = "NEW GAME";
    newGameButton.style.padding = "12px 30px";
    newGameButton.style.fontSize = "18px";
    newGameButton.style.border = "none";
    newGameButton.style.borderRadius = "10px";
    newGameButton.style.backgroundColor = "#4facfe"; // Màu xanh gradient dịu mắt
    newGameButton.style.color = "white";
    newGameButton.style.cursor = "pointer";
    newGameButton.style.transition = "all 0.3s ease-in-out";
    newGameButton.onmouseover = function () {
        newGameButton.style.backgroundColor = "#007BFF"; // Xanh đậm hơn khi hover
        newGameButton.style.transform = "scale(1.1)";
    };
    newGameButton.onmouseout = function () {
        newGameButton.style.backgroundColor = "#4facfe";
        newGameButton.style.transform = "scale(1)";
    };
    newGameButton.onclick = function () {
        callResetAPI(); // Reset lại game
        location.reload();
    };
    buttonContainer.appendChild(newGameButton);

    // Nút BACK TO HOME
    let backButton = document.createElement("button");
    backButton.innerText = "BACK TO HOME";
    backButton.style.padding = "12px 30px";
    backButton.style.fontSize = "18px";
    backButton.style.border = "none";
    backButton.style.borderRadius = "10px";
    backButton.style.backgroundColor = "#f0932b"; // Màu cam dịu phù hợp với giao diện
    backButton.style.color = "white";
    backButton.style.cursor = "pointer";
    backButton.style.transition = "all 0.3s ease-in-out";
    backButton.onmouseover = function () {
        backButton.style.backgroundColor = "#e17055"; // Cam đậm hơn khi hover
        backButton.style.transform = "scale(1.1)";
    };
    backButton.onmouseout = function () {
        backButton.style.backgroundColor = "#f0932b";
        backButton.style.transform = "scale(1)";
    };
    backButton.onclick = function () {
        callResetAPI();
        window.location.href = "home.html";
    };
    buttonContainer.appendChild(backButton);

    dialog.appendChild(buttonContainer); // Thêm các nút vào dialog
    document.body.appendChild(dialog);
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
    if (swapButton) {
        swapButton.disabled = true; // Vô hiệu hóa nút
        swapButton.style.opacity = "0.5"; // Làm mờ nút để hiển thị trạng thái không hoạt động
    }
    // Cập nhật chỉ báo lượt chơi
    updateTurnIndicator();
    // Vẽ lại bảng Hex để phản ánh trạng thái mới
    resetBoardUI_afterswap();
    // Hiển thị thông báo swap
    //alert(`Swap đã được thực hiện! Người chơi hiện tại: ${currentPlayer === 1 ? 'Đỏ' : 'Xanh dương'}`);
}
function checkAndDisableSwap() {
    for (let row = 0; row < rows; row++) {
        for (let col = 0; col < cols; col++) {
            if (board[row][col] === 2) { // Phát hiện nước đi của người chơi thứ 2
                const swapButton = document.getElementById("swap-button");
                swapButton.disabled = true; // Vô hiệu hóa nút Swap
                swapButton.style.opacity = "0.5"; // Làm mờ nút Swap
                console.log("Player 2 has made a move. Swap is now disabled.");
                return;
            }
        }
    }
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

function redraw() {
    for (let row = 0; row < rows; row++) {
        for (let col = 0; col < cols; col++) {
            let x = col * dx + row * (dx / 2);
            let y = row * dy;

            if (board[row][col] === 1) {
                drawHexagon(x, y, 'red');
            } else if (board[row][col] === 2) {
                drawHexagon(x, y, 'blue');
            } else {
                drawHexagon(x, y, 'white');
            }
        }
    }
}
function sendMoveToAPI(row, col, player) {
    fetch('/api/move', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ row, col, player })
    }).then(response => response.json())
        .then(data => {
            if (data.winner !== 0) {
                showWinDialog(data.winner);
            } else if (currentPlayer === 2) {
                getAIMove();
            }
        });
}

function getAIMove() {
    fetch('/api/aimove', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(board)
    }).then(response => response.json())
        .then(aiMove => {
            if (aiMove && aiMove.length === 2) {
                board[aiMove[0]][aiMove[1]] = 2;
                currentPlayer = 1;
            }
        });
}
function startAIGame() {
    document.getElementById("turn-indicator").innerText = "Your Turn";
    board = Array.from({ length: boardSize }, () => Array(boardSize).fill(0));
    playerTurn = true;
    updateBoard();
}
