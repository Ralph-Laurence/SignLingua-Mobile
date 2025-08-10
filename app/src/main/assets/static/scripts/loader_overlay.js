let $loadingOverlay;
let IS_FULLY_LOADED = false;
let loadingStartTime = 0;
const MIN_LOADING_TIME = 1200; // Ensure overlay is visible for at least 1200ms

$(document).ready(function() {
    $loadingOverlay = $('.loading-overlay');

    if ($loadingOverlay.length === 0) {
        console.warn("Warning: .loading-overlay element not found!");
    }
});

$(window).on('load', function() {
    setTimeout(() => {
        hideLoading();
        IS_FULLY_LOADED = true;
    }, MIN_LOADING_TIME);
});

// Function to show loading overlay with minimum visibility
function showLoading(type = 'page', fade = false) {
    if (!$loadingOverlay || $loadingOverlay.length === 0) return;

    loadingStartTime = Date.now(); // Track when loading started

    if (type === 'page') {
        $loadingOverlay.removeClass('action-load').addClass('page-load');
        $loadingOverlay.find('.progress-bar').removeClass('bg-orange-500').addClass('bg-primary-700');
    } else if (type === 'action') {
        $loadingOverlay.removeClass('page-load').addClass('action-load');
        $loadingOverlay.find('.progress-bar').removeClass('bg-primary-700').addClass('bg-orange-500');
    }

    fade ? $loadingOverlay.fadeIn(300) : $loadingOverlay.show();
}

// Function to hide loading overlay **only if 1800ms has passed**
function hideLoading() {
    if (!$loadingOverlay || $loadingOverlay.length === 0) return;

    const elapsedTime = Date.now() - loadingStartTime;
    const remainingTime = MIN_LOADING_TIME - elapsedTime;

    if (remainingTime > 0) {
        setTimeout(() => {
            $loadingOverlay.fadeOut(300);
        }, remainingTime);
    } else {
        $loadingOverlay.fadeOut(300);
    }
}
