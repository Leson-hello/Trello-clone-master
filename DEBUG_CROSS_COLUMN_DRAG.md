# Debug Cross-Column Drag & Drop

## 🔍 **Debug được thêm vào:**

### **Log Tags để filter:**

- `CrossColumnDrag` - Tất cả logs về drag & drop giữa cột
- `TaskListActivity` - Logs về việc di chuyển card
- `BoardState` - Trạng thái board trước/sau khi di chuyển

### **Chi tiết logs:**

#### **1. CrossColumnDrag logs:**

```
=== INIT CrossColumnItemTouchHelper for column X ===
Total columns: Y
Column 0: 'Column Name' (Z cards)
```

#### **2. Drag process logs:**

```
=== DRAG STARTED ===
Column: X, Position: Y
Available target columns: Z

onChildDraw: dX=150.0, dY=10.0, totalDistance=300.0, threshold=150.0
*** INTER-COLUMN DRAG DETECTED ***
Target column changed to: Y
Target column name: 'Target Column'
Direction: RIGHT/LEFT
```

#### **3. Move execution logs:**

```
=== CLEAR VIEW ===
*** EXECUTING CROSS-COLUMN MOVE ***
From column X to column Y
Card position: Z
Positions validated. Calling moveCardBetweenColumns...
```

## 🧪 **Cách test debug:**

### **Bước 1: Chuẩn bị**

1. Tạo ít nhất 2 cột trong project
2. Thêm 1-2 cards vào mỗi cột
3. Mở Android Studio Logcat
4. Filter theo tag: `CrossColumnDrag`

### **Bước 2: Test drag trong cột**

1. **Long press** một card
2. **Kéo lên/xuống** trong cùng cột
3. **Quan sát logs**: Sẽ thấy `onMove: Swapping within column`

### **Bước 3: Test drag giữa cột**

1. **Long press** một card
2. **Kéo qua trái/phải** ít nhất **150 pixel**
3. **Quan sát logs**: Sẽ thấy `*** INTER-COLUMN DRAG DETECTED ***`
4. **Thả card**
5. **Quan sát logs**: Sẽ thấy `*** EXECUTING CROSS-COLUMN MOVE ***`

## 📊 **Threshold hiện tại:**

- **DRAG_THRESHOLD**: 150 pixel
- **SwipeThreshold**: 0.2f
- **MoveThreshold**: 0.2f

## 🔧 **Điều chỉnh nếu cần:**

### **Giảm threshold nếu khó kéo:**

```kotlin
private val DRAG_THRESHOLD = 100f // Giảm từ 150f
```

### **Tăng sensitivity:**

```kotlin
override fun getSwipeThreshold(...): Float = 0.1f // Giảm từ 0.2f
```

## 🚨 **Vấn đề có thể gặp:**

### **1. Không detect inter-column drag:**

- **Nguyên nhân**: Threshold quá cao
- **Giải pháp**: Giảm `DRAG_THRESHOLD`
- **Log để check**: `onChildDraw: dX=XXX`

### **2. Detect nhưng không move:**

- **Nguyên nhân**: Validation positions fail
- **Giải pháp**: Check logs `Invalid positions`
- **Log để check**: `BoardState` logs

### **3. Move nhưng không update UI:**

- **Nguyên nhân**: Firestore update fail
- **Giải pháp**: Check network connection
- **Log để check**: `updating Firestore...`

## 📝 **Expected Log Flow:**

### **Successful cross-column drag:**

```
CrossColumnDrag: === DRAG STARTED ===
CrossColumnDrag: onChildDraw: dX=160.0...
CrossColumnDrag: *** INTER-COLUMN DRAG DETECTED ***
CrossColumnDrag: Target column changed to: 1
CrossColumnDrag: === CLEAR VIEW ===
CrossColumnDrag: *** EXECUTING CROSS-COLUMN MOVE ***
TaskListActivity: === MOVE CARD BETWEEN COLUMNS ===
BoardState: === BOARD STATE (BEFORE MOVE) ===
BoardState: === BOARD STATE (AFTER MOVE) ===
TaskListActivity: Card drag moved successfully...
```

## ⚡ **Quick Debug Checklist:**

- [ ] Logs xuất hiện khi long press?
- [ ] `dX` value đạt threshold (>150)?
- [ ] `INTER-COLUMN DRAG DETECTED` xuất hiện?
- [ ] `targetColumn` được set đúng?
- [ ] `EXECUTING CROSS-COLUMN MOVE` xuất hiện?
- [ ] `BEFORE/AFTER MOVE` board state khác nhau?
- [ ] Firestore update thành công?

**Nếu bất kỳ step nào fail, đó là nguyên nhân!** 🎯