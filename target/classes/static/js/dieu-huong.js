(function () {
    const nav = document.getElementById('vfMainNav');
    const menuToggle = document.querySelector('.vf-menu-toggle');

    if (!nav || !menuToggle) {
        return;
    }

    const body = document.body;
    const submenuItems = Array.from(nav.querySelectorAll('.has-submenu'));
    const desktopMedia = window.matchMedia('(min-width: 993px)');
    const hoverTimers = new WeakMap();

    const setExpanded = (item, expanded) => {
        const toggle = item.querySelector('.vf-nav__toggle');
        if (toggle) {
            toggle.setAttribute('aria-expanded', String(expanded));
        }
    };

    const clearHoverTimer = (item) => {
        const existing = hoverTimers.get(item);
        if (existing) {
            clearTimeout(existing);
            hoverTimers.delete(item);
        }
    };

    const closeSubmenuItem = (item) => {
        item.classList.remove('is-open');
        setExpanded(item, false);
    };

    const closeSubmenus = (exception) => {
        submenuItems.forEach(subItem => {
            if (!exception || subItem !== exception) {
                clearHoverTimer(subItem);
                closeSubmenuItem(subItem);
            }
        });
    };

    const openSubmenuItem = (item) => {
        closeSubmenus(item);
        item.classList.add('is-open');
        setExpanded(item, true);
    };

    const closeMenu = () => {
        nav.classList.remove('is-open');
        body.classList.remove('vf-nav-open');
        menuToggle.setAttribute('aria-expanded', 'false');
        closeSubmenus();
    };

    const openMenu = () => {
        nav.classList.add('is-open');
        body.classList.add('vf-nav-open');
        menuToggle.setAttribute('aria-expanded', 'true');
    };

    menuToggle.addEventListener('click', () => {
        if (nav.classList.contains('is-open')) {
            closeMenu();
        } else {
            openMenu();
        }
    });

    submenuItems.forEach(item => {
        const toggle = item.querySelector('.vf-nav__toggle');
        const submenuPanel = item.querySelector('.vf-submenu');

        if (toggle) {
            toggle.addEventListener('click', event => {
                if (desktopMedia.matches) {
                    return;
                }

                event.preventDefault();
                const willOpen = !item.classList.contains('is-open');

                if (willOpen) {
                    openSubmenuItem(item);
                } else {
                    closeSubmenuItem(item);
                }
            });

            toggle.addEventListener('focus', () => {
                if (desktopMedia.matches) {
                    openSubmenuItem(item);
                }
            });
        }

        item.addEventListener('mouseenter', () => {
            if (!desktopMedia.matches) {
                return;
            }

            clearHoverTimer(item);
            openSubmenuItem(item);
        });

        item.addEventListener('mouseleave', () => {
            if (!desktopMedia.matches) {
                return;
            }

            clearHoverTimer(item);
            hoverTimers.set(item, window.setTimeout(() => {
                closeSubmenuItem(item);
            }, 180));
        });

        if (submenuPanel) {
            submenuPanel.addEventListener('mouseenter', () => {
                if (!desktopMedia.matches) {
                    return;
                }

                clearHoverTimer(item);
            });

            submenuPanel.addEventListener('mouseleave', () => {
                if (!desktopMedia.matches) {
                    return;
                }

                clearHoverTimer(item);
                hoverTimers.set(item, window.setTimeout(() => {
                    closeSubmenuItem(item);
                }, 180));
            });
        }
    });

    document.addEventListener('click', event => {
        if (!nav.classList.contains('is-open')) {
            return;
        }

        const isClickInside = nav.contains(event.target) || menuToggle.contains(event.target);
        if (!isClickInside) {
            closeMenu();
        }
    });

    document.addEventListener('keydown', event => {
        if (event.key === 'Escape' && nav.classList.contains('is-open')) {
            closeMenu();
        }
    });

    nav.querySelectorAll('.vf-nav__link').forEach(link => {
        link.addEventListener('click', () => {
            if (nav.classList.contains('is-open')) {
                closeMenu();
            }
        });
    });

    const handleResize = () => {
        if (desktopMedia.matches) {
            closeMenu();
        }
    };

    window.addEventListener('resize', handleResize);
})();

// Search box hover functionality
(function() {
    const searchToggle = document.getElementById('searchToggle');
    const searchBox = document.getElementById('searchBox');
    const searchInput = document.getElementById('searchInput');
    
    if (!searchToggle || !searchBox) {
        return;
    }
    
    let hoverTimer;
    let isHovering = false;
    
    const showSearchBox = () => {
        clearTimeout(hoverTimer);
        searchBox.style.display = 'block';
        // Force reflow to trigger transition
        searchBox.offsetHeight;
        searchBox.classList.add('show');
        if (searchInput) {
            setTimeout(() => searchInput.focus(), 150);
        }
    };
    
    const hideSearchBox = (delay = 0) => {
        clearTimeout(hoverTimer);
        const hide = () => {
            searchBox.classList.remove('show');
            setTimeout(() => {
                if (!isHovering) {
                    searchBox.style.display = 'none';
                }
            }, 300); // Wait for transition to complete
        };
        
        if (delay > 0) {
            hoverTimer = setTimeout(() => {
                if (!isHovering) {
                    hide();
                }
            }, delay);
        } else {
            hide();
        }
    };
    
    // Hover on search button
    searchToggle.addEventListener('mouseenter', () => {
        isHovering = true;
        showSearchBox();
    });
    
    searchToggle.addEventListener('mouseleave', () => {
        isHovering = false;
        hideSearchBox(200); // Delay 200ms before hiding
    });
    
    // Hover on search box
    searchBox.addEventListener('mouseenter', () => {
        isHovering = true;
        clearTimeout(hoverTimer);
    });
    
    searchBox.addEventListener('mouseleave', () => {
        isHovering = false;
        hideSearchBox(200); // Delay 200ms before hiding
    });
    
    // Click on search button (toggle)
    searchToggle.addEventListener('click', (e) => {
        e.preventDefault();
        const isVisible = searchBox.style.display !== 'none';
        if (isVisible) {
            hideSearchBox();
        } else {
            showSearchBox();
        }
    });
    
    // Close when clicking outside
    document.addEventListener('click', (e) => {
        if (!searchBox.contains(e.target) && !searchToggle.contains(e.target)) {
            isHovering = false;
            hideSearchBox();
        }
    });
    
    // Autocomplete/Suggestions
    if (searchInput) {
        let suggestionsContainer = null;
        let debounceTimer;
        
        const createSuggestionsContainer = () => {
            if (!suggestionsContainer) {
                suggestionsContainer = document.createElement('div');
                suggestionsContainer.className = 'vf-search-suggestions';
                suggestionsContainer.style.cssText = 'position: absolute; top: calc(100% + 8px); left: 0; right: 0; background: white; border: 1px solid #e2e8f0; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); max-height: 300px; overflow-y: auto; z-index: 1001; display: none;';
                const searchForm = searchBox.querySelector('.vf-search-form');
                if (searchForm) {
                    searchForm.style.position = 'relative';
                    searchForm.appendChild(suggestionsContainer);
                } else {
                    searchBox.appendChild(suggestionsContainer);
                }
            }
            return suggestionsContainer;
        };
        
        const hideSuggestions = () => {
            if (suggestionsContainer) {
                suggestionsContainer.style.display = 'none';
            }
        };
        
        const showSuggestions = (suggestions) => {
            const container = createSuggestionsContainer();
            container.innerHTML = '';
            
            if (suggestions.length === 0) {
                container.style.display = 'none';
                return;
            }
            
            suggestions.forEach(product => {
                const item = document.createElement('a');
                item.href = '/san-pham/' + product.slug;
                item.className = 'vf-suggestion-item';
                item.style.cssText = 'display: flex; align-items: center; padding: 12px; text-decoration: none; color: #1e293b; border-bottom: 1px solid #f1f5f9; transition: background 0.2s;';
                item.innerHTML = `
                    <img src="${product.image || '/image/evo200.jpg'}" 
                         alt="${product.name}" 
                         style="width: 50px; height: 50px; object-fit: cover; border-radius: 4px; margin-right: 12px;"
                         onerror="this.src='/image/evo200.jpg'">
                    <div style="flex: 1;">
                        <div style="font-weight: 600; margin-bottom: 4px;">${product.name}</div>
                        <div style="font-size: 14px; color: #2563eb; font-weight: 600;">
                            ${new Intl.NumberFormat('vi-VN').format(product.price)} ₫
                        </div>
                    </div>
                `;
                item.addEventListener('mouseenter', () => {
                    item.style.background = '#f8fafc';
                });
                item.addEventListener('mouseleave', () => {
                    item.style.background = 'transparent';
                });
                container.appendChild(item);
            });
            
            container.style.display = 'block';
        };
        
        searchInput.addEventListener('input', (e) => {
            const keyword = e.target.value.trim();
            
            clearTimeout(debounceTimer);
            
            if (keyword.length < 2) {
                hideSuggestions();
                return;
            }
            
            // Debounce: đợi 300ms sau khi người dùng ngừng gõ
            debounceTimer = setTimeout(() => {
                fetch(`/api/products/suggestions?keyword=${encodeURIComponent(keyword)}`)
                    .then(response => response.json())
                    .then(suggestions => {
                        showSuggestions(suggestions);
                    })
                    .catch(error => {
                        console.error('Error fetching suggestions:', error);
                        hideSuggestions();
                    });
            }, 300);
        });
        
        // Hide suggestions when clicking outside
        document.addEventListener('click', (e) => {
            if (!searchBox.contains(e.target)) {
                hideSuggestions();
            }
        });
        
        // Hide suggestions on form submit
        const searchForm = searchBox.querySelector('form');
        if (searchForm) {
            searchForm.addEventListener('submit', () => {
                hideSuggestions();
            });
        }
    }
})();

// Account dropdown menu functionality
(function() {
    const accountToggle = document.getElementById('accountToggle');
    const accountMenu = document.getElementById('accountMenu');
    
    if (!accountToggle || !accountMenu) {
        return;
    }
    
    let isHovering = false;
    let hoverTimer;
    
    const showMenu = () => {
        clearTimeout(hoverTimer);
        accountMenu.classList.add('show');
    };
    
    const hideMenu = (delay = 0) => {
        clearTimeout(hoverTimer);
        if (delay > 0) {
            hoverTimer = setTimeout(() => {
                if (!isHovering) {
                    accountMenu.classList.remove('show');
                }
            }, delay);
        } else {
            accountMenu.classList.remove('show');
        }
    };
    
    // Click toggle
    accountToggle.addEventListener('click', (e) => {
        e.preventDefault();
        const isVisible = accountMenu.classList.contains('show');
        if (isVisible) {
            hideMenu();
        } else {
            showMenu();
        }
    });
    
    // Hover on button
    accountToggle.addEventListener('mouseenter', () => {
        isHovering = true;
        showMenu();
    });
    
    accountToggle.addEventListener('mouseleave', () => {
        isHovering = false;
        hideMenu(200);
    });
    
    // Hover on menu
    accountMenu.addEventListener('mouseenter', () => {
        isHovering = true;
        clearTimeout(hoverTimer);
    });
    
    accountMenu.addEventListener('mouseleave', () => {
        isHovering = false;
        hideMenu(200);
    });
    
    // Close when clicking outside
    document.addEventListener('click', (e) => {
        if (!accountMenu.contains(e.target) && !accountToggle.contains(e.target)) {
            isHovering = false;
            hideMenu();
        }
    });
})();


