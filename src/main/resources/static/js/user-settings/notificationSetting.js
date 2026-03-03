document.addEventListener('DOMContentLoaded', () => {
    const backBtn = document.querySelector('.back-btn');
    const perfStartNoti = document.getElementById('perfStartNoti');
    const dmNoti = document.getElementById('dmNoti');

    const API_URL = '/api/users/settings/notifications';

    backBtn.addEventListener('click', () => {
        history.back();
    });

    init();

    async function init() {
        await loadNotificationSettings();
        bindEvents();
    }

    function bindEvents() {
        perfStartNoti.addEventListener('change', saveSettings);
        dmNoti.addEventListener('change', saveSettings);
    }

    async function loadNotificationSettings() {
        try {
            const response = await fetch(API_URL, {
                credentials: 'include',
            });

            if (!response.ok) {
                throw new Error('알림 설정 조회 실패');
            }

            const result = await response.json();

            if (!result.success) {
                throw new Error(result.message);
            }

            const data = result.data;

            perfStartNoti.checked = data.performanceStartAlert;
            dmNoti.checked = data.dmAlert;

        } catch (error) {
            console.error('조회 오류:', error.message);
            alert('알림 설정을 불러오지 못했습니다.');
        }
    }

    async function saveSettings() {
        try {
            const response = await fetch(API_URL, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({
                    performanceStartAlert: perfStartNoti.checked,
                    dmAlert: dmNoti.checked
                })
            });

            if (!response.ok) {
                throw new Error('알림 설정 저장 실패');
            }

            const result = await response.json();

            if (!result.success) {
                throw new Error(result.message);
            }

        } catch (error) {
            console.error('저장 오류:', error.message);
            alert('알림 설정 저장 중 오류가 발생했습니다.');
        }
    }
});
