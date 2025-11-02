# 📋 Hướng dẫn sử dụng Drag & Drop

## ✅ **Tính năng đã triển khai:**

### 🎯 **Kéo thả trong cùng cột:**

- **Long press** (giữ) card để bắt đầu kéo
- **Kéo lên/xuống** để thay đổi thứ tự trong cột
- **Thả** để hoàn tất - tự động lưu vào Firestore

### 🔄 **Sẵn sàng cho kéo thả giữa các cột:**

- Đã có `CrossColumnItemTouchHelper` cho kéo giữa cột
- Đã có `moveCardBetweenColumns()` method
- Đã có logic phát hiện cột đích và cập nhật status

## 🛠️ **Cách test:**

### **Test kéo thả trong cột:**

1. Mở app, vào một project
2. **Long press** một task card
3. **Kéo lên/xuống** trong cùng cột
4. **Thả** - card sẽ đổi vị trí và tự động lưu

### **Bật kéo thả giữa cột (nếu cần):**

Trong `TaskListItemAdapter.kt`, thay đổi:

```kotlin
// Từ:
val itemTouchHelper = ItemTouchHelper(SimpleItemTouchHelper(...))

// Thành:  
val crossColumnHelper = CrossColumnItemTouchHelper(...)
crossColumnHelper.attachToRecyclerView(...)
```

## 🎨 **Visual Feedback:**

- **Khi kéo**: Card trở nên mờ và to hơn
- **Elevation**: Card nổi lên trên các card khác
- **Thả**: Card trở về trạng thái bình thường

## 🔧 **Technical Details:**

### **Files quan trọng:**

- `SimpleItemTouchHelper.kt` - Kéo trong cột
- `CrossColumnItemTouchHelper.kt` - Kéo giữa cột (sẵn sàng)
- `TaskListActivity.kt` - Logic xử lý di chuyển
- `TaskListItemAdapter.kt` - Tích hợp ItemTouchHelper

### **Flow hoạt động:**

```
Long Press → Start Drag → Move Card → Release → Update UI → Save to Firestore
```

## 🎯 **Status tự động:**

Khi di chuyển giữa cột, status sẽ tự động cập nhật:

- **"Cần làm"** → `PENDING`
- **"Đang tiến hành"** → `IN_PROGRESS`
- **"Hoàn thành"** → `COMPLETED`

## ⚡ **Ready to use!**

Tính năng kéo thả trong cột đã sẵn sàng sử dụng. Kéo thả giữa cột có thể bật bằng cách thay đổi
helper class như hướng dẫn trên!