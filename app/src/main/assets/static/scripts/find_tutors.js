const disabilityBadges = {
    0: '',
    1: '../static/images/badge_deaf_s.png',
    2: '../static/images/badge_mute_s.png',
    3: '../static/images/badge_deafmute_s.png'
}

let $inputSearchTerm = null;
let $disabilitySelect = null;
let selectedDisabilityFilter = null;
let fabClearFilters;

$(document).ready(() => {

    window.history.pushState(null, "", window.location.href);
    window.onpopstate = function () {
        history.pushState(null, "", window.location.href);
    };

    $inputSearchTerm = $('#input-search-term');
    $disabilitySelect = new bootstrap.Dropdown($('#btn-select-disability'));
    fabClearFilters = $('#fab-clear-filter');

    fabClearFilters.on('click', () => {

        // Reset filters
        selectedDisabilityFilter = null;
        $inputSearchTerm.val('');
        $('#btn-select-disability .filter-text').text('Filter');
        fabClearFilters.hide();

        // For pagination
        let page = $('meta[name="currentpage"]').attr('content');

        if (window.FindTutorJsBridge)
        {
            window.FindTutorJsBridge.onFindTutors(null, page, selectedDisabilityFilter);
        }
    });
    
    if ($inputSearchTerm)
    {
        $inputSearchTerm.on('focus', () => $disabilitySelect.hide());
    }

    $('#btn-back').on("click", function () {

        if (window.FindTutorJsBridge) {
            window.FindTutorJsBridge.onGoBack();
        }
    });

    // Attach Click Events to Pagination Buttons
    $(document).on("click", '.page-link', function (e)
    {
        e.preventDefault();

        // Prevent clicks on the active page
        if ($(this).parent().hasClass("active")) {
            return;
        }

        let page = $(this).data("page");

        if (page && window.FindTutorJsBridge)
        {
            window.FindTutorJsBridge.onFindTutors(null, page, null);
        }
    })
    
    .on('click', ".dropdown-item.disability-filter-item", function()
    {
        let disability = $(this).data('value');
        let page = $('meta[name="currentpage"]').attr('content');
        let keyword = $inputSearchTerm.val();

        let filterValueDisplay = getDisabilityFilterDisplay(disability);
        $('#btn-select-disability .filter-text').text(filterValueDisplay);

        selectedDisabilityFilter = disability;

        if (window.FindTutorJsBridge)
        {
            window.FindTutorJsBridge.onFindTutors(keyword, page, disability);
        }
        
    })
    
    .on('click', ".search-item", function() {
        let id = $(this).data('id');
        if (window.FindTutorJsBridge)
        {
            window.FindTutorJsBridge.onStalkProfile(id);
        }
    });

    $('#btn-search').on('click', function()
    {
        let keyword = $inputSearchTerm.val();

        if (keyword.trim() === '')
        {
            AlertWarn('Please enter a valid search term.');
            return;
        }

        let page = $('meta[name="currentpage"]').attr('content');

        if (window.FindTutorJsBridge)
        {
            window.FindTutorJsBridge.onFindTutors(keyword, page, selectedDisabilityFilter);
        }
    });
});

function displayTutors(data)
{
    if (IS_FULLY_LOADED)
        showLoading('action');

    console.log("This is called")
    if (data === '' || data === undefined || data === null)
    {
        return;
    }

    console.log(data)

    let obj = JSON.parse(data);
    let wrapper = $('#search-results');
    wrapper.empty();

    $('meta[name="currentpage"]').attr('content', obj.current_page);
    $('#lbl-total-tutors').text(obj.total);

    if (obj.total === 0)
    {
        wrapper.html(`
            <div class="w-100 text-center pt-4">
                <img src="../static/images/cat_not_found.png" class="cat_icn_not_found mb-3">
                <h6>No matching records</h6>
                <p class="text-muted text-14 mx-auto w-50">We searched everywhere, but couldn't find anything.</p>
            </div>`);

        fabClearFilters.show();
        hideLoading();
        return;
    }

    if (obj.searchTerm || obj.filterValue)
    {
        let filterValueDisplay = getDisabilityFilterDisplay(obj.filterValue) || 'All';

        wrapper.append(`<div class="w-100 flex-center overflow-hidden mb-3 filter-queries">
                    <div class="filter-query me-auto heading">Search Results</div>
                    <div class="filter-query flex-fill search-term text-truncate">
                        <i class="fas fa-search"></i>:
                        <span class="query-search-term ms-1">${obj.searchTerm || 'All'}</span>
                    </div>
                    <div class="filter-query ms-auto text-truncate disability-filter">
                        <i class="fas fa-filter"></i>:
                        <span class="query-disability-filter ms-1"> ${filterValueDisplay}</span>
                    </div>
                </div>`);

        fabClearFilters.show();
    }

    obj.data.forEach(tutor => {

        console.log(`${tutor.name} -> ${tutor.disability}`)
        let badge = '';

        if (tutor.disability > 0)
            badge = `<img src="${disabilityBadges[tutor.disability]}" class="ms-auto disability-badge">`;

        let template = `
        <div class="search-item shadow mb-2" data-id="${tutor.id}">
            <div class="search-item-avatar-container">
                <img src="${tutor.photo}" class="search-item-avatar">
            </div>
            <div class="search-item-details-container overflow-hidden">
                <div class="w-100 d-flex align-items-center gap-3 mb-2">
                    <h6 class="text-truncate search-item-fullname w-100 m-0">${tutor.name}</h6>
                    ${badge}
                </div>
                <p class="text-truncate search-item-contact-details w-100">
                    <span class="username">@${tutor.username}</span> | ${tutor.email}
                </p>
            </div>
        </div>`;

        wrapper.append(template);
    });

    // Create the result counter
    let resultsText = `<p class="text-14 text-muted mt-5 mb-3 w-100 text-center">Showing ${obj.from}-${obj.to} of ${obj.total} results</p>`;
    wrapper.append(resultsText)

    // Create the pagination template as a jQuery object
    let $paginationTemplate = $(`
        <nav>
            <ul class="pagination justify-content-center">
            </ul>
        </nav>`);

    // Select the <ul> inside the template
    let $paginationList = $paginationTemplate.find('ul');

    // Render Pagination Controls
    obj.links.forEach(link => {
        let isDisabled = link.url == null || !link.url ? "disabled" : "";
        let isActive = link.active ? "active" : "";
        let label = link.label;

        if (label.toLowerCase().includes('prev'))
            label = `<i class="fas fa-left-long"></i>`;

        else if (label.toLowerCase().includes('next'))
            label = `<i class="fas fa-right-long"></i>`;

        let pageItem = $(`
            <li class="page-item ${isDisabled} ${isActive}">
                <a class="page-link" href="#" data-page="${extractPageNumber(link.url)}">${label}</a>
            </li>
        `);

        $paginationList.append(pageItem);
    });

    wrapper.append($paginationTemplate);

    // If page is fully loaded, it closes the loader automatically during first load
    // Now if we want to do long running action, we can manually show the loader
    // then hide it manually
    if (IS_FULLY_LOADED)
        hideLoading();
}

// Helper Function to Extract Page Number from URL
function extractPageNumber(url) {
    if (!url) return null;
    let match = url.match(/page=(\d+)/);
    return match ? match[1] : null;
}

function getDisabilityFilterDisplay(filterValue)
{
    let filterValueDisplay = $(`.disability-filter-item[data-value="${filterValue}"]`)
        .find('.disability-filter-name')
        .text();

    return filterValueDisplay;
}