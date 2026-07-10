(function () {
  'use strict';

  var firebaseConfig = {
    apiKey: "AIzaSyDfQWeDxjrGoWDu4m4qmipMh-QoNEdXdmQ",
    authDomain: "appbanhang-b322a.firebaseapp.com",
    projectId: "appbanhang-b322a",
    storageBucket: "appbanhang-b322a.firebasestorage.app",
    messagingSenderId: "383401607828",
    appId: "1:383401607828:web:2f0dadce45dc95f1b4ea81",
    measurementId: "G-NVD7EZZ4D5"
  };

  firebase.initializeApp(firebaseConfig);

  angular.module('adminApp', [])
    .controller('ProductAdminController', ['$scope', function ($scope) {
      var vm = this;
      var db = firebase.firestore();
      var productsRef = db.collection('products');
      var usersRef = db.collection('users');
      var ordersRef = db.collection('orders');
      var fallbackOrdersRef = db.collection('oders');
      var vouchersRef = db.collection('vouchers');
      var bannersRef = db.collection('banners');

      vm.products = [];
      vm.users = [];
      vm.orders = [];
      vm.vouchers = [];
      vm.banners = [];
      vm.form = emptyProductForm();
      vm.voucherForm = emptyVoucherForm();
      vm.bannerForm = emptyBannerForm();
      vm.keyword = '';
      vm.userKeyword = '';
      vm.orderKeyword = '';
      vm.voucherKeyword = '';
      vm.bannerKeyword = '';
      vm.editingId = null;
      vm.editingVoucherId = null;
      vm.editingBannerId = null;
      vm.activeMenu = 'dashboard';
      vm.sidebarCollapsed = false;
      vm.mobileSidebarOpen = false;
      vm.loading = false;
      vm.usersLoading = false;
      vm.ordersLoading = false;
      vm.vouchersLoading = false;
      vm.bannersLoading = false;
      vm.saving = false;
      vm.voucherSaving = false;
      vm.bannerSaving = false;
      vm.error = '';
      vm.message = '';
      vm.selectedOrder = null;

      vm.pageTitle = pageTitle;
      vm.pageSubtitle = pageSubtitle;
      vm.loadProducts = loadProducts;
      vm.loadUsers = loadUsers;
      vm.loadOrders = loadOrders;
      vm.loadVouchers = loadVouchers;
      vm.loadBanners = loadBanners;
      vm.refreshCurrent = refreshCurrent;
      vm.exportCurrentExcel = exportCurrentExcel;
      vm.filteredProducts = filteredProducts;
      vm.filteredUsers = filteredUsers;
      vm.filteredOrders = filteredOrders;
      vm.filteredVouchers = filteredVouchers;
      vm.filteredBanners = filteredBanners;
      vm.saveProduct = saveProduct;
      vm.editProduct = editProduct;
      vm.deleteProduct = deleteProduct;
      vm.resetForm = resetForm;
      vm.toggleUserLock = toggleUserLock;
      vm.deleteUser = deleteUser;
      vm.saveVoucher = saveVoucher;
      vm.editVoucher = editVoucher;
      vm.deleteVoucher = deleteVoucher;
      vm.resetVoucherForm = resetVoucherForm;
      vm.saveBanner = saveBanner;
      vm.editBanner = editBanner;
      vm.deleteBanner = deleteBanner;
      vm.resetBannerForm = resetBannerForm;
      vm.updateOrderStatus = updateOrderStatus;
      vm.totalStock = totalStock;
      vm.totalRevenue = totalRevenue;
      vm.averageOrderValue = averageOrderValue;
      vm.pendingOrders = pendingOrders;
      vm.completedOrders = completedOrders;
      vm.revenueSeries = revenueSeries;
      vm.revenueMax = revenueMax;
      vm.chartBarStyle = chartBarStyle;
      vm.recentOrders = recentOrders;
      vm.orderAmount = orderAmount;
      vm.orderItemCount = orderItemCount;
      vm.orderItems = orderItems;
      vm.orderCustomer = orderCustomer;
      vm.orderAddress = orderAddress;
      vm.orderItemTotal = orderItemTotal;
      vm.statusSummary = statusSummary;
      vm.formatDate = formatDate;
      vm.selectMenu = selectMenu;
      vm.toggleSidebar = toggleSidebar;
      vm.toggleMobileSidebar = toggleMobileSidebar;
      vm.closeMobileSidebar = closeMobileSidebar;
      vm.selectOrder = selectOrder;
      vm.closeOrderDetail = closeOrderDetail;
      vm.stopAction = stopAction;

      loadAll();

      function loadAll() {
        loadProducts();
        loadUsers();
        loadOrders();
        loadVouchers();
        loadBanners();
      }

      function loadProducts() {
        vm.loading = true;
        vm.error = '';
        return productsRef.orderBy('id', 'asc').get()
          .then(function (snapshot) {
            vm.products = snapshot.docs.map(function (doc) {
              var data = withDocId(doc);
              normalizeImage(data);
              return data;
            });
          })
          .catch(function (error) {
            vm.error = 'Lỗi tải sản phẩm: ' + error.message;
          })
          .finally(function () {
            vm.loading = false;
            $scope.$applyAsync();
          });
      }

      function loadUsers() {
        vm.usersLoading = true;
        return usersRef.get()
          .then(function (snapshot) {
            vm.users = snapshot.docs.map(withDocId).sort(sortByNameOrEmail);
          })
          .catch(function (error) {
            vm.error = 'Lỗi tải user: ' + error.message;
          })
          .finally(function () {
            vm.usersLoading = false;
            $scope.$applyAsync();
          });
      }

      function loadOrders() {
        vm.ordersLoading = true;
        return ordersRef.get()
          .then(function (snapshot) {
            if (!snapshot.empty) {
              vm.orders = snapshot.docs.map(function (doc) {
                return withDocId(doc, 'orders');
              }).sort(sortOrders);
              keepSelectedOrder();
              return null;
            }
            return fallbackOrdersRef.get().then(function (fallbackSnapshot) {
              vm.orders = fallbackSnapshot.docs.map(function (doc) {
                return withDocId(doc, 'oders');
              }).sort(sortOrders);
              keepSelectedOrder();
            });
          })
          .catch(function (error) {
            vm.error = 'Lỗi tải đơn hàng: ' + error.message;
          })
          .finally(function () {
            vm.ordersLoading = false;
            $scope.$applyAsync();
          });
      }

      function loadVouchers() {
        vm.vouchersLoading = true;
        return vouchersRef.get()
          .then(function (snapshot) {
            if (snapshot.empty) {
              return ensureVouchersCollection().then(function () {
                vm.vouchers = [];
              });
            }
            vm.vouchers = snapshot.docs
              .filter(function (doc) {
                return doc.id !== '_meta';
              })
              .map(withDocId)
              .sort(sortVouchers);
          })
          .catch(function (error) {
            vm.error = firebaseError('Lỗi tải/tạo collection vouchers', error);
          })
          .finally(function () {
            vm.vouchersLoading = false;
            $scope.$applyAsync();
          });
      }

      function ensureVouchersCollection() {
        return vouchersRef.doc('_meta').set({
          collection: 'vouchers',
          description: 'Metadata document used to initialize the vouchers collection.',
          createdAt: new Date(),
          updatedAt: new Date()
        }, { merge: true });
      }

      function refreshCurrent() {
        clearNotices();
        if (vm.activeMenu === 'dashboard') {
          loadProducts();
          loadOrders();
          return;
        }
        if (vm.activeMenu === 'products') loadProducts();
        if (vm.activeMenu === 'approval') loadOrders();
        if (vm.activeMenu === 'vouchers') loadVouchers();
        if (vm.activeMenu === 'banners') loadBanners();
        if (vm.activeMenu === 'users') loadUsers();
      }

      function exportCurrentExcel() {
        clearNotices();
        var config = exportConfig();
        if (!config || !config.rows || config.rows.length === 0) {
          vm.error = 'Không có dữ liệu để xuất Excel.';
          return;
        }

        downloadCsv(config.fileName, config.headers, config.rows);
        vm.message = 'Đã xuất file Excel: ' + config.fileName;
      }

      function exportConfig() {
        if (vm.activeMenu === 'products') {
          return {
            fileName: buildExportFileName('san-pham'),
            headers: ['ID', 'Tên sản phẩm', 'Danh mục', 'Thương hiệu', 'Giá', 'Tồn kho', 'Ảnh', 'Mô tả'],
            rows: filteredProducts().map(function (product) {
              return [
                product.id || product._docId || '',
                product.name || '',
                product.category || '',
                product.brand || '',
                Number(product.price) || 0,
                Number(product.stock) || 0,
                product.imageUrl || product.thumbnailUrl || '',
                product.description || ''
              ];
            })
          };
        }

        if (vm.activeMenu === 'approval') {
          return {
            fileName: buildExportFileName('don-hang'),
            headers: ['Mã đơn', 'User', 'Số sản phẩm', 'Tổng tiền', 'Thanh toán', 'Trạng thái', 'Ngày đặt', 'Địa chỉ'],
            rows: filteredOrders().map(function (order) {
              return [
                order.orderId || order._docId || '',
                order.userEmail || order.userId || '',
                order.items && order.items.length ? order.items.length : 0,
                orderAmount(order),
                order.paymentMethod || '',
                order.orderStatus || '',
                formatDate(order.orderDate || order.createdAt),
                order.deliveryAddress || ''
              ];
            })
          };
        }

        if (vm.activeMenu === 'vouchers') {
          return {
            fileName: buildExportFileName('voucher'),
            headers: ['Mã', 'Tên voucher', 'Loại giảm', 'Giá trị', 'Đơn tối thiểu', 'Giảm tối đa', 'Ngày bắt đầu', 'Ngày kết thúc', 'Đã dùng', 'Giới hạn', 'Trạng thái'],
            rows: filteredVouchers().map(function (voucher) {
              return [
                voucher.code || voucher._docId || '',
                voucher.title || '',
                voucher.type || '',
                Number(voucher.value) || 0,
                Number(voucher.minOrder) || 0,
                Number(voucher.maxDiscount) || 0,
                voucher.startDate || '',
                voucher.endDate || '',
                Number(voucher.usedCount) || 0,
                Number(voucher.usageLimit) || 0,
                voucher.isActive !== false ? 'Active' : 'Tắt'
              ];
            })
          };
        }

        if (vm.activeMenu === 'banners') {
          return {
            fileName: buildExportFileName('banner'),
            headers: ['ID', 'Tiêu đề', 'Phụ đề', 'Ảnh', 'Thứ tự', 'Loại', 'Action URL', 'Trạng thái'],
            rows: filteredBanners().map(function (banner) {
              return [
                banner.id || banner._docId || '',
                banner.title || '',
                banner.subtitle || '',
                banner.imageUrl || '',
                Number(banner.displayOrder) || 0,
                banner.type || '',
                banner.actionUrl || '',
                banner.isActive !== false ? 'Đang hiển thị' : 'Ẩn'
              ];
            })
          };
        }

        if (vm.activeMenu === 'users') {
          return {
            fileName: buildExportFileName('user'),
            headers: ['Doc ID', 'ID', 'Họ tên', 'Email', 'Số điện thoại', 'Địa chỉ', 'Trạng thái'],
            rows: filteredUsers().map(function (user) {
              return [
                user._docId || '',
                user.id || '',
                user.fullName || '',
                user.email || '',
                user.phoneNumber || '',
                user.address || user.city || user.province || '',
                isUserLocked(user) ? 'Đã khóa' : 'Hoạt động'
              ];
            })
          };
        }

        return {
          fileName: buildExportFileName('dashboard'),
          headers: ['Chỉ số', 'Giá trị'],
          rows: [
            ['Tổng sản phẩm', vm.products.length || 0],
            ['Tổng tồn kho', totalStock()],
            ['Tổng đơn hàng', vm.orders.length || 0],
            ['Đơn đang xử lý', pendingOrders()],
            ['Đơn đã hoàn thành', completedOrders()],
            ['Tổng doanh thu', totalRevenue()],
            ['Giá trị trung bình/đơn', averageOrderValue()],
            ['Doanh thu cao nhất/ngày', revenueMax()]
          ].concat(revenueSeries().map(function (point) {
            return ['Doanh thu ngày ' + point.label, point.value];
          }))
        };
      }

      function downloadCsv(fileName, headers, rows) {
        var lines = [headers].concat(rows).map(function (row) {
          return row.map(csvCell).join(',');
        });
        var csv = '\ufeff' + lines.join('\r\n');
        var blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
        var link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = fileName;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        setTimeout(function () {
          URL.revokeObjectURL(link.href);
        }, 0);
      }

      function csvCell(value) {
        if (value == null) return '""';
        return '"' + String(value).replace(/"/g, '""') + '"';
      }

      function buildExportFileName(prefix) {
        return 'smarteshop-' + prefix + '-' + dateKey(new Date()) + '.csv';
      }

      function filteredProducts() {
        var keyword = normalizeKeyword(vm.keyword);
        if (!keyword) return vm.products;
        return vm.products.filter(function (product) {
          return contains(product.name, keyword)
            || contains(product.category, keyword)
            || contains(product.brand, keyword);
        });
      }

      function filteredUsers() {
        var keyword = normalizeKeyword(vm.userKeyword);
        if (!keyword) return vm.users;
        return vm.users.filter(function (user) {
          return contains(user.fullName, keyword)
            || contains(user.email, keyword)
            || contains(user.phoneNumber, keyword)
            || contains(user.city, keyword)
            || contains(user.province, keyword);
        });
      }

      function filteredOrders() {
        var keyword = normalizeKeyword(vm.orderKeyword);
        if (!keyword) return vm.orders;
        return vm.orders.filter(function (order) {
          return contains(order.orderId, keyword)
            || contains(order._docId, keyword)
            || contains(order.userId, keyword)
            || contains(order.orderStatus, keyword)
            || contains(order.paymentMethod, keyword)
            || contains(order.deliveryAddress, keyword)
            || contains(order.promoCode, keyword);
        });
      }

      function filteredVouchers() {
        var keyword = normalizeKeyword(vm.voucherKeyword);
        if (!keyword) return vm.vouchers;
        return vm.vouchers.filter(function (voucher) {
          return contains(voucher.code, keyword)
            || contains(voucher.title, keyword)
            || contains(voucher.description, keyword);
        });
      }

      function loadBanners() {
        vm.bannersLoading = true;
        return bannersRef.get()
          .then(function (snapshot) {
            vm.banners = snapshot.docs.map(withDocId).sort(sortBanners);
          })
          .catch(function (error) {
            vm.error = firebaseError('Lỗi tải banners', error);
          })
          .finally(function () {
            vm.bannersLoading = false;
            $scope.$applyAsync();
          });
      }

      function filteredBanners() {
        var keyword = normalizeKeyword(vm.bannerKeyword);
        if (!keyword) return vm.banners;
        return vm.banners.filter(function (banner) {
          return contains(banner.title, keyword)
            || contains(banner.subtitle, keyword)
            || contains(banner.type, keyword)
            || contains(banner.actionUrl, keyword);
        });
      }

      function saveProduct() {
        vm.error = '';
        vm.message = '';

        if (!vm.form.name || !vm.form.name.trim()) {
          vm.error = 'Vui lòng nhập tên sản phẩm.';
          return;
        }
        if (!vm.form.imageUrl || !vm.form.imageUrl.trim()) {
          vm.error = 'Vui lòng nhập link ảnh sản phẩm.';
          return;
        }

        var product = {
          id: vm.editingId || nextProductId(),
          name: vm.form.name.trim(),
          category: clean(vm.form.category),
          brand: clean(vm.form.brand),
          price: Number(vm.form.price) || 0,
          stock: Number(vm.form.stock) || 0,
          imageUrl: clean(vm.form.imageUrl),
          thumbnailUrl: clean(vm.form.imageUrl),
          imageUrls: [clean(vm.form.imageUrl)],
          description: clean(vm.form.description),
          rating: Number(vm.form.rating) || 5,
          reviewCount: Number(vm.form.reviewCount) || 0,
          discount: Number(vm.form.discount) || 0,
          promotion: clean(vm.form.promotion),
          color: clean(vm.form.color),
          isNew: vm.editingId ? !!vm.form.isNew : true
        };

        vm.saving = true;
        productsRef.doc(String(product.id)).set(product, { merge: true })
          .then(function () {
            vm.message = 'Đã lưu sản phẩm #' + product.id + ' lên Firebase.';
            resetForm();
            return loadProducts();
          })
          .catch(function (error) {
            vm.error = 'Lỗi lưu sản phẩm: ' + error.message;
          })
          .finally(function () {
            vm.saving = false;
            $scope.$applyAsync();
          });
      }

      function editProduct(product) {
        vm.editingId = product.id;
        vm.form = {
          name: product.name || '',
          category: product.category || '',
          brand: product.brand || '',
          price: Number(product.price) || 0,
          stock: Number(product.stock) || 0,
          imageUrl: product.imageUrl || product.thumbnailUrl || '',
          description: product.description || '',
          rating: Number(product.rating) || 5,
          reviewCount: Number(product.reviewCount) || 0,
          discount: Number(product.discount) || 0,
          promotion: product.promotion || '',
          color: product.color || '',
          isNew: !!product.isNew
        };
        vm.activeMenu = 'products';
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }

      function deleteProduct(product) {
        if (!window.confirm('Xóa "' + product.name + '" khỏi Firebase?')) return;

        vm.error = '';
        vm.message = '';
        productsRef.where('id', '==', product.id).get()
          .then(function (snapshot) {
            var deletions = snapshot.docs.map(function (doc) {
              return doc.ref.delete();
            });
            if (deletions.length === 0 && product._docId) {
              deletions.push(productsRef.doc(product._docId).delete());
            }
            return Promise.all(deletions);
          })
          .then(function () {
            vm.message = 'Đã xóa sản phẩm.';
            if (vm.editingId === product.id) resetForm();
            return loadProducts();
          })
          .catch(function (error) {
            vm.error = 'Lỗi xóa sản phẩm: ' + error.message;
          })
          .finally(function () {
            $scope.$applyAsync();
          });
      }

      function saveVoucher() {
        vm.error = '';
        vm.message = '';

        if (!vm.voucherForm.code || !vm.voucherForm.code.trim()) {
          vm.error = 'Vui lòng nhập mã voucher.';
          return;
        }
        if (!vm.voucherForm.title || !vm.voucherForm.title.trim()) {
          vm.error = 'Vui lòng nhập tên voucher.';
          return;
        }

        var code = vm.voucherForm.code.trim().toUpperCase();
        var docId = vm.editingVoucherId || code;
        var voucher = {
          code: code,
          title: vm.voucherForm.title.trim(),
          type: vm.voucherForm.type || 'percent',
          value: Number(vm.voucherForm.value) || 0,
          minOrder: Number(vm.voucherForm.minOrder) || 0,
          maxDiscount: Number(vm.voucherForm.maxDiscount) || 0,
          startDate: clean(vm.voucherForm.startDate),
          endDate: clean(vm.voucherForm.endDate),
          usageLimit: Number(vm.voucherForm.usageLimit) || 0,
          usedCount: Number(vm.voucherForm.usedCount) || 0,
          isActive: !!vm.voucherForm.isActive,
          description: clean(vm.voucherForm.description),
          updatedAt: new Date()
        };

        if (!vm.editingVoucherId) {
          voucher.createdAt = new Date();
        }

        vm.voucherSaving = true;
        vouchersRef.doc(docId).set(voucher, { merge: true })
          .then(function () {
            vm.message = 'Đã lưu voucher ' + code + ' vào collection vouchers.';
            resetVoucherForm();
            return loadVouchers();
          })
          .catch(function (error) {
            vm.error = firebaseError('Lỗi lưu voucher', error);
          })
          .finally(function () {
            vm.voucherSaving = false;
            $scope.$applyAsync();
          });
      }

      function editVoucher(voucher) {
        vm.editingVoucherId = voucher._docId;
        vm.voucherForm = {
          code: voucher.code || voucher._docId || '',
          title: voucher.title || '',
          type: voucher.type || 'percent',
          value: Number(voucher.value) || 0,
          minOrder: Number(voucher.minOrder) || 0,
          maxDiscount: Number(voucher.maxDiscount) || 0,
          startDate: voucher.startDate || '',
          endDate: voucher.endDate || '',
          usageLimit: Number(voucher.usageLimit) || 0,
          usedCount: Number(voucher.usedCount) || 0,
          isActive: voucher.isActive !== false,
          description: voucher.description || ''
        };
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }

      function deleteVoucher(voucher) {
        var label = voucher.code || voucher._docId;
        if (!window.confirm('Xóa voucher "' + label + '" khỏi Firebase?')) return;

        vm.error = '';
        vm.message = '';
        vouchersRef.doc(voucher._docId).delete()
          .then(function () {
            vm.message = 'Đã xóa voucher ' + label + '.';
            if (vm.editingVoucherId === voucher._docId) resetVoucherForm();
            return loadVouchers();
          })
          .catch(function (error) {
            vm.error = firebaseError('Lỗi xóa voucher', error);
          })
          .finally(function () {
            $scope.$applyAsync();
          });
      }

      function saveBanner() {
        vm.error = '';
        vm.message = '';

        if (!vm.bannerForm.title || !vm.bannerForm.title.trim()) {
          vm.error = 'Vui lòng nhập tiêu đề banner.';
          return;
        }
        if (!vm.bannerForm.imageUrl || !vm.bannerForm.imageUrl.trim()) {
          vm.error = 'Vui lòng nhập link ảnh banner.';
          return;
        }

        var currentBanner = editingBanner();
        var bannerId = vm.editingBannerId || String(nextBannerId());
        var bannerNumericId = Number(vm.bannerForm.id)
          || Number(currentBanner && currentBanner.id)
          || Number(bannerId)
          || nextBannerId();
        var banner = {
          id: bannerNumericId,
          title: vm.bannerForm.title.trim(),
          subtitle: clean(vm.bannerForm.subtitle),
          imageUrl: clean(vm.bannerForm.imageUrl),
          actionUrl: clean(vm.bannerForm.actionUrl),
          displayOrder: Number(vm.bannerForm.displayOrder) || 0,
          type: clean(vm.bannerForm.type) || 'PROMO',
          backgroundColor: clean(vm.bannerForm.backgroundColor),
          isActive: !!vm.bannerForm.isActive,
          updatedAt: new Date()
        };

        if (!vm.editingBannerId) {
          banner.createdAt = new Date();
        }

        vm.bannerSaving = true;
        bannersRef.doc(bannerId).set(banner, { merge: true })
          .then(function () {
            vm.message = 'Đã lưu banner "' + banner.title + '" vào collection banners.';
            resetBannerForm();
            return loadBanners();
          })
          .catch(function (error) {
            vm.error = firebaseError('Lỗi lưu banner', error);
          })
          .finally(function () {
            vm.bannerSaving = false;
            $scope.$applyAsync();
          });
      }

      function editBanner(banner) {
        vm.editingBannerId = banner._docId;
        vm.bannerForm = {
          id: Number(banner.id) || Number(banner._docId) || 0,
          title: banner.title || '',
          subtitle: banner.subtitle || '',
          imageUrl: banner.imageUrl || '',
          actionUrl: banner.actionUrl || '',
          displayOrder: Number(banner.displayOrder) || 0,
          type: banner.type || 'PROMO',
          backgroundColor: banner.backgroundColor || '',
          isActive: banner.isActive !== false
        };
        vm.activeMenu = 'banners';
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }

      function deleteBanner(banner) {
        var label = banner.title || banner._docId;
        if (!window.confirm('Xóa banner "' + label + '" khỏi Firebase?')) return;

        vm.error = '';
        vm.message = '';
        bannersRef.doc(banner._docId).delete()
          .then(function () {
            vm.message = 'Đã xóa banner ' + label + '.';
            if (vm.editingBannerId === banner._docId) resetBannerForm();
            return loadBanners();
          })
          .catch(function (error) {
            vm.error = firebaseError('Lỗi xóa banner', error);
          })
          .finally(function () {
            $scope.$applyAsync();
          });
      }

      function updateOrderStatus(order, status) {
        vm.error = '';
        vm.message = '';
        db.collection(order._collection || 'orders').doc(order._docId).set({
          orderStatus: status,
          updatedAt: new Date()
        }, { merge: true })
          .then(function () {
            vm.message = 'Đã cập nhật đơn #' + (order.orderId || order._docId) + ' thành "' + status + '".';
            if (vm.selectedOrder && vm.selectedOrder._docId === order._docId) {
              vm.selectedOrder.orderStatus = status;
            }
            return loadOrders();
          })
          .catch(function (error) {
            vm.error = 'Lỗi cập nhật đơn hàng: ' + error.message;
          })
          .finally(function () {
            $scope.$applyAsync();
          });
      }

      function toggleUserLock(user) {
        var locked = !isUserLocked(user);
        var status = locked ? 'locked' : 'active';
        var label = user.fullName || user.email || user._docId;

        vm.error = '';
        vm.message = '';
        usersRef.doc(user._docId).set({
          isLocked: locked,
          accountStatus: status,
          updatedAt: new Date()
        }, { merge: true })
          .then(function () {
            vm.message = (locked ? 'Đã khóa tài khoản ' : 'Đã mở khóa tài khoản ') + label + '.';
            return loadUsers();
          })
          .catch(function (error) {
            vm.error = firebaseError('Lỗi cập nhật trạng thái user', error);
          })
          .finally(function () {
            $scope.$applyAsync();
          });
      }

      function deleteUser(user) {
        var label = user.fullName || user.email || user._docId;
        if (!window.confirm('Xóa user "' + label + '" khỏi collection users?')) return;

        vm.error = '';
        vm.message = '';
        usersRef.doc(user._docId).delete()
          .then(function () {
            vm.message = 'Đã xóa user ' + label + ' khỏi Firestore.';
            return loadUsers();
          })
          .catch(function (error) {
            vm.error = firebaseError('Lỗi xóa user', error);
          })
          .finally(function () {
            $scope.$applyAsync();
          });
      }

      function resetForm() {
        vm.editingId = null;
        vm.form = emptyProductForm();
      }

      function resetVoucherForm() {
        vm.editingVoucherId = null;
        vm.voucherForm = emptyVoucherForm();
      }

      function resetBannerForm() {
        vm.editingBannerId = null;
        vm.bannerForm = emptyBannerForm();
      }

      function totalStock() {
        return vm.products.reduce(function (sum, product) {
          return sum + (Number(product.stock) || 0);
        }, 0);
      }

      function totalRevenue() {
        return vm.orders.reduce(function (sum, order) {
          return sum + orderAmount(order);
        }, 0);
      }

      function averageOrderValue() {
        if (!vm.orders.length) return 0;
        return totalRevenue() / vm.orders.length;
      }

      function pendingOrders() {
        return vm.orders.filter(function (order) {
          var status = String(order.orderStatus || '').toLowerCase();
          return !status
            || status.indexOf('xử lý') !== -1
            || status.indexOf('xu ly') !== -1
            || status.indexOf('xác nhận') !== -1
            || status.indexOf('xac nhan') !== -1;
        }).length;
      }

      function completedOrders() {
        return vm.orders.filter(function (order) {
          var status = String(order.orderStatus || '').toLowerCase();
          return status.indexOf('đã giao') !== -1
            || status.indexOf('da giao') !== -1
            || status.indexOf('hoàn thành') !== -1
            || status.indexOf('hoan thanh') !== -1;
        }).length;
      }

      function revenueSeries() {
        var days = [];
        var today = new Date();
        for (var i = 6; i >= 0; i--) {
          var date = new Date(today.getFullYear(), today.getMonth(), today.getDate() - i);
          days.push({
            key: dateKey(date),
            label: date.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' }),
            value: 0
          });
        }

        var dayMap = days.reduce(function (map, day) {
          map[day.key] = day;
          return map;
        }, {});

        vm.orders.forEach(function (order) {
          var orderDate = orderDateValue(order);
          if (!orderDate) return;
          var key = dateKey(orderDate);
          if (dayMap[key]) {
            dayMap[key].value += orderAmount(order);
          }
        });

        return days;
      }

      function revenueMax() {
        return revenueSeries().reduce(function (max, item) {
          return Math.max(max, item.value);
        }, 0);
      }

      function chartBarStyle(value) {
        var max = revenueMax();
        var percent = max <= 0 ? 0 : Math.max(8, Math.round((value / max) * 100));
        return { height: percent + '%' };
      }

      function recentOrders() {
        return vm.orders.slice().sort(sortOrders).slice(0, 5);
      }

      function orderAmount(order) {
        return Number(order && (order.totalAmount || order.total || order.amount)) || 0;
      }

      function orderItemCount(order) {
        return orderItems(order).length;
      }

      function orderItems(order) {
        return order && angular.isArray(order.items) ? order.items : [];
      }

      function orderCustomer(order) {
        if (!order) return 'Chưa có';
        return order.customerName
          || order.fullName
          || order.receiverName
          || order.userEmail
          || order.userUid
          || order.userId
          || 'Chưa có';
      }

      function orderAddress(order) {
        if (!order) return 'Chưa có';
        return order.deliveryAddress
          || order.shippingAddress
          || order.address
          || order.receiverAddress
          || 'Chưa có';
      }

      function orderItemTotal(item) {
        if (!item) return 0;
        return Number(item.total)
          || Number(item.totalPrice)
          || (Number(item.price) || 0) * (Number(item.quantity) || 0);
      }

      function statusSummary() {
        var summary = {
          total: vm.orders.length,
          pending: pendingOrders(),
          completed: completedOrders()
        };
        summary.cancelled = vm.orders.filter(function (order) {
          var status = String(order.orderStatus || '').toLowerCase();
          return status.indexOf('hủy') !== -1 || status.indexOf('huy') !== -1;
        }).length;
        return summary;
      }

      function pageTitle() {
        var titles = {
          dashboard: 'Dashboard',
          products: 'Quản lý sản phẩm',
          approval: 'Quản lý đơn hàng',
          vouchers: 'Quản lý voucher',
          banners: 'Quản lý banners',
          users: 'Quản lý user'
        };
        return titles[vm.activeMenu] || 'Dashboard';
      }

      function pageSubtitle() {
        var subtitles = {
          dashboard: 'Tổng quan sản phẩm, tồn kho và doanh thu.',
          products: 'Thêm, sửa, xóa sản phẩm trong collection products.',
          approval: 'Xem chi tiết và cập nhật trạng thái đơn hàng từ collection orders.',
          vouchers: 'Tạo, sửa, xóa voucher trong collection vouchers.',
          banners: 'Tạo, sửa, xóa banner trang chủ trong collection banners.',
          users: 'Danh sách người dùng từ collection users.'
        };
        return subtitles[vm.activeMenu] || '';
      }

      function selectMenu(menu, event) {
        if (event && event.preventDefault) {
          event.preventDefault();
        }
        if (vm.activeMenu !== menu) {
          clearNotices();
        }
        vm.activeMenu = menu;
        closeMobileSidebar();
        refreshCurrent();
      }

      function selectOrder(order) {
        vm.selectedOrder = order;
      }

      function closeOrderDetail() {
        vm.selectedOrder = null;
      }

      function stopAction(event) {
        if (!event) return;
        if (event.preventDefault) event.preventDefault();
        if (event.stopPropagation) event.stopPropagation();
      }

      function keepSelectedOrder() {
        if (!vm.selectedOrder) return;
        var selectedDocId = vm.selectedOrder._docId;
        vm.selectedOrder = vm.orders.find(function (order) {
          return order._docId === selectedDocId;
        }) || null;
      }

      function clearNotices() {
        vm.error = '';
        vm.message = '';
      }

      function toggleSidebar() {
        vm.sidebarCollapsed = !vm.sidebarCollapsed;
      }

      function toggleMobileSidebar() {
        vm.mobileSidebarOpen = !vm.mobileSidebarOpen;
      }

      function closeMobileSidebar() {
        vm.mobileSidebarOpen = false;
      }

      function nextProductId() {
        return vm.products.reduce(function (max, product) {
          return Math.max(max, Number(product.id) || 0);
        }, 0) + 1;
      }

      function nextBannerId() {
        return vm.banners.reduce(function (max, banner) {
          return Math.max(max, Number(banner.id) || Number(banner._docId) || 0);
        }, 0) + 1;
      }

      function editingBanner() {
        if (!vm.editingBannerId) return null;
        return vm.banners.find(function (banner) {
          return banner._docId === vm.editingBannerId;
        }) || null;
      }

      function emptyProductForm() {
        return {
          name: '',
          category: '',
          brand: '',
          price: 0,
          stock: 0,
          imageUrl: '',
          description: '',
          rating: 5,
          reviewCount: 0,
          discount: 0,
          promotion: '',
          color: '',
          isNew: true
        };
      }

      function emptyVoucherForm() {
        return {
          code: '',
          title: '',
          type: 'percent',
          value: 0,
          minOrder: 0,
          maxDiscount: 0,
          startDate: '',
          endDate: '',
          usageLimit: 0,
          usedCount: 0,
          isActive: true,
          description: ''
        };
      }

      function emptyBannerForm() {
        return {
          id: 0,
          title: '',
          subtitle: '',
          imageUrl: '',
          actionUrl: '',
          displayOrder: 0,
          type: 'PROMO',
          backgroundColor: '',
          isActive: true
        };
      }

      function withDocId(doc, collectionName) {
        var data = doc.data() || {};
        data._docId = doc.id;
        if (collectionName) data._collection = collectionName;
        return data;
      }

      function normalizeImage(product) {
        if (!product.imageUrl && product.thumbnailUrl) product.imageUrl = product.thumbnailUrl;
        if (!product.imageUrl && product.imageUrls && product.imageUrls.length) product.imageUrl = product.imageUrls[0];
      }

      function formatDate(value) {
        if (!value) return 'Chưa có';
        var date = value.toDate ? value.toDate() : new Date(value);
        if (Number.isNaN(date.getTime())) return String(value);
        return date.toLocaleDateString('vi-VN');
      }

      function sortByNameOrEmail(a, b) {
        return String(a.fullName || a.email || '').localeCompare(String(b.fullName || b.email || ''));
      }

      function sortOrders(a, b) {
        return dateValue(b.orderDate || b.createdAt) - dateValue(a.orderDate || a.createdAt);
      }

      function sortVouchers(a, b) {
        return String(a.code || a._docId || '').localeCompare(String(b.code || b._docId || ''));
      }

      function sortBanners(a, b) {
        return (Number(a.displayOrder) || 0) - (Number(b.displayOrder) || 0);
      }

      function dateValue(value) {
        if (!value) return 0;
        var date = value.toDate ? value.toDate() : new Date(value);
        return Number.isNaN(date.getTime()) ? 0 : date.getTime();
      }

      function orderDateValue(order) {
        var value = order && (order.orderDate || order.createdAt || order.updatedAt);
        if (!value) return null;
        var date = value.toDate ? value.toDate() : new Date(value);
        return Number.isNaN(date.getTime()) ? null : date;
      }

      function dateKey(date) {
        return [
          date.getFullYear(),
          String(date.getMonth() + 1).padStart(2, '0'),
          String(date.getDate()).padStart(2, '0')
        ].join('-');
      }

      function normalizeKeyword(value) {
        return (value || '').toString().toLowerCase().trim();
      }

      function contains(value, keyword) {
        return value != null && String(value).toLowerCase().indexOf(keyword) !== -1;
      }

      function clean(value) {
        return value == null ? '' : String(value).trim();
      }

      function isUserLocked(user) {
        return user && (user.isLocked === true || user.accountStatus === 'locked');
      }

      function firebaseError(prefix, error) {
        if (error && error.code === 'permission-denied') {
          return prefix + ': Web-admin đã kết nối tới Firebase, nhưng Firestore Rules đang chặn quyền đọc/ghi. Hãy cấp quyền cho collection tương ứng trong Firebase Console > Firestore Database > Rules.';
        }
        return prefix + ': ' + (error && error.message ? error.message : 'Không rõ lỗi.');
      }
    }]);
})();
