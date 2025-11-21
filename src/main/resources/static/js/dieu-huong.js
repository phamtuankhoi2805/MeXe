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


