/**
 * 프로필 드롭다운 토글 - fragments/profile, fragments/performer/profile 공통
 */
(function () {
  function initProfileDropdown(wrap) {
    var toggle = wrap.querySelector('[data-profile-toggle]');
    var menu = wrap.querySelector('[data-profile-menu]');
    if (!toggle || !menu) return;
    if (wrap._profileInited) return;
    wrap._profileInited = true;
    toggle.addEventListener('click', function (e) {
      e.preventDefault();
      e.stopPropagation();
      wrap.classList.toggle('is-open');
    });
    document.addEventListener('click', function () {
      wrap.classList.remove('is-open');
    });
    menu.addEventListener('click', function (e) {
      e.stopPropagation();
    });
  }

  function initAll() {
    document.querySelectorAll('.profile-dropdown-wrap').forEach(initProfileDropdown);
  }

  function run() {
    initAll();
    // 상단바 등 나중에 있을 수 있는 영역 위해 한 번 더 시도
    if (document.readyState === 'complete') return;
    setTimeout(initAll, 100);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', run);
  } else {
    run();
  }
  window.addEventListener('load', initAll);
})();
