/**
 * Cart Management JavaScript
 * Xử lý thêm sản phẩm vào giỏ hàng, quản lý localStorage, đồng bộ với server
 */

// Cart helper functions
const CartHelper = {
    // Lấy giỏ hàng từ localStorage
    getLocalCart: function() {
        try {
            const cart = localStorage.getItem('cart');
            return cart ? JSON.parse(cart) : [];
        } catch (e) {
            console.error('Error reading cart from localStorage:', e);
            return [];
        }
    },

    // Lưu giỏ hàng vào localStorage
    saveLocalCart: function(cart) {
        try {
            localStorage.setItem('cart', JSON.stringify(cart));
        } catch (e) {
            console.error('Error saving cart to localStorage:', e);
        }
    },

    // Thêm sản phẩm vào localStorage cart
    addToLocalCart: function(productId, colorId, quantity) {
        const cart = this.getLocalCart();
        const existingIndex = cart.findIndex(item => 
            item.productId === productId && item.colorId === colorId
        );

        if (existingIndex >= 0) {
            // Đã có trong giỏ -> cộng dồn
            cart[existingIndex].quantity += quantity;
        } else {
            // Chưa có -> thêm mới
            cart.push({
                productId: productId,
                colorId: colorId,
                quantity: quantity,
                addedAt: new Date().toISOString()
            });
        }

        this.saveLocalCart(cart);
        this.updateCartBadge();
        return cart;
    },

    // Đếm tổng số lượng sản phẩm trong giỏ hàng
    getCartItemCount: function() {
        const cart = this.getLocalCart();
        return cart.reduce((total, item) => total + item.quantity, 0);
    },

    // Cập nhật badge số lượng trên icon giỏ hàng
    updateCartBadge: function() {
        const count = this.getCartItemCount();
        let badge = document.getElementById('cartBadge');
        
        if (!badge && count > 0) {
            // Tạo badge nếu chưa có
            const cartLink = document.querySelector('a[href="/gio-hang"], a[href*="gio-hang"]');
            if (cartLink) {
                badge = document.createElement('span');
                badge.id = 'cartBadge';
                badge.className = 'cart-badge';
                badge.style.cssText = 'position: absolute; top: -6px; right: -6px; background: #dc2626; color: white; border-radius: 50%; width: 18px; height: 18px; font-size: 11px; font-weight: 600; display: flex; align-items: center; justify-content: center;';
                cartLink.style.position = 'relative';
                cartLink.appendChild(badge);
            }
        }
        
        if (badge) {
            if (count > 0) {
                badge.textContent = count > 99 ? '99+' : count;
                badge.style.display = 'flex';
            } else {
                badge.style.display = 'none';
            }
        }
    },

    // Đồng bộ localStorage cart lên server
    syncCartToServer: function(userId) {
        const localCart = this.getLocalCart();
        if (localCart.length === 0) {
            return Promise.resolve([]);
        }

        return fetch(`/api/cart/sync/${userId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                items: localCart.map(item => ({
                    productId: item.productId,
                    colorId: item.colorId,
                    quantity: item.quantity
                }))
            })
        })
        .then(res => {
            if (!res.ok) {
                throw new Error('Failed to sync cart');
            }
            return res.json();
        })
        .then(data => {
            if (data.success) {
                // Xóa localStorage sau khi đồng bộ thành công
                localStorage.removeItem('cart');
                this.updateCartBadge();
                return data.cart || [];
            }
            throw new Error(data.message || 'Sync failed');
        })
        .catch(err => {
            console.error('Error syncing cart:', err);
            return [];
        });
    },

    // Lấy số lượng giỏ hàng từ server
    getServerCartCount: function(userId) {
        return fetch(`/api/cart/count/${userId}`)
            .then(res => res.json())
            .then(data => data.count || 0)
            .catch(err => {
                console.error('Error getting cart count:', err);
                return 0;
            });
    },

    // Cập nhật badge từ server
    updateCartBadgeFromServer: function(userId) {
        this.getServerCartCount(userId)
            .then(count => {
                let badge = document.getElementById('cartBadge');
                
                if (!badge && count > 0) {
                    const cartLink = document.querySelector('a[href="/gio-hang"], a[href*="gio-hang"]');
                    if (cartLink) {
                        badge = document.createElement('span');
                        badge.id = 'cartBadge';
                        badge.className = 'cart-badge';
                        badge.style.cssText = 'position: absolute; top: -6px; right: -6px; background: #dc2626; color: white; border-radius: 50%; width: 18px; height: 18px; font-size: 11px; font-weight: 600; display: flex; align-items: center; justify-content: center;';
                        cartLink.style.position = 'relative';
                        cartLink.appendChild(badge);
                    }
                }
                
                if (badge) {
                    if (count > 0) {
                        badge.textContent = count > 99 ? '99+' : count;
                        badge.style.display = 'flex';
                    } else {
                        badge.style.display = 'none';
                    }
                }
            });
    }
};

/**
 * Thêm sản phẩm vào giỏ hàng
 * @param {number} productId - ID sản phẩm
 * @param {number|null} colorId - ID màu sắc (optional)
 * @param {number} quantity - Số lượng
 * @param {number|null} userId - ID người dùng (null nếu chưa đăng nhập)
 * @param {boolean} isAuthenticated - Trạng thái đăng nhập
 */
function addToCart(productId, colorId, quantity, userId, isAuthenticated) {
    if (!productId || !quantity || quantity <= 0) {
        alert('Thông tin sản phẩm không hợp lệ');
        return Promise.reject('Invalid product data');
    }

    // Lấy button để cập nhật UI
    const btn = event?.target?.closest('.btn-buy-now') || 
                document.querySelector('.btn-buy-now');
    
    if (btn) {
        btn.disabled = true;
        const btnSpan = btn.querySelector('span');
        if (btnSpan) {
            btnSpan.textContent = 'Đang xử lý...';
        } else {
            btn.textContent = 'Đang xử lý...';
        }
    }

    const resetButton = () => {
        if (btn) {
            btn.disabled = false;
            const btnSpan = btn.querySelector('span');
            if (btnSpan) {
                btnSpan.textContent = 'Mua Ngay';
            } else {
                btn.textContent = 'Mua Ngay';
            }
        }
    };

    const showSuccess = () => {
        if (btn) {
            const btnSpan = btn.querySelector('span');
            if (btnSpan) {
                btnSpan.textContent = '✓ Đã thêm!';
            } else {
                btn.textContent = '✓ Đã thêm!';
            }
            btn.style.background = 'linear-gradient(135deg, #16a34a 0%, #15803d 100%)';
            setTimeout(() => {
                resetButton();
                btn.style.background = '';
            }, 2000);
        }
    };

    // Nếu đã đăng nhập -> gọi API server
    if (isAuthenticated && userId) {
        return fetch('/api/cart/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                productId: productId,
                colorId: colorId,
                quantity: quantity
            })
        })
        .then(res => {
            if (!res.ok) {
                return res.json().then(data => {
                    throw new Error(data.message || 'Không thể thêm vào giỏ hàng');
                });
            }
            return res.json();
        })
        .then(data => {
            if (data.success) {
                showSuccess();
                // Cập nhật badge từ server
                CartHelper.updateCartBadgeFromServer(userId);
                return data;
            } else {
                throw new Error(data.message || 'Không thể thêm vào giỏ hàng');
            }
        })
        .catch(err => {
            console.error('Error adding to cart:', err);
            alert(err.message || 'Có lỗi xảy ra khi thêm vào giỏ hàng');
            resetButton();
            throw err;
        });
    } else {
        // Chưa đăng nhập -> lưu vào localStorage
        CartHelper.addToLocalCart(productId, colorId, quantity);
        showSuccess();
        
        // Hiển thị thông báo yêu cầu đăng nhập
        setTimeout(() => {
            if (confirm('Bạn chưa đăng nhập. Đăng nhập để đồng bộ giỏ hàng trên các thiết bị?')) {
                window.location.href = '/login';
            }
        }, 1000);
        
        return Promise.resolve({ success: true, message: 'Đã thêm vào giỏ hàng tạm thời' });
    }
}

// Khởi tạo khi trang load
document.addEventListener('DOMContentLoaded', function() {
    // Cập nhật badge từ localStorage nếu chưa đăng nhập
    const isAuthenticated = /*[[${#authentication != null && #authentication.isAuthenticated() && #authentication.name != 'anonymousUser'}]]*/ false;
    if (!isAuthenticated) {
        CartHelper.updateCartBadge();
    }
});

