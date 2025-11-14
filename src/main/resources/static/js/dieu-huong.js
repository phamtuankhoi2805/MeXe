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


