// const data = {
// 	"status": 200,
// 	"message": "",
// 	"content": {
// 		"currentUser": "Tarzan Cruz",
// 		"authToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtV01vSk1FNFFqIiwibmFtZSI6IlRhcnphbiBDcnV6IiwiaWF0IjoxNzU5Mzc4NDgzLCJleHAiOjE3NTkzODIwODN9.YLXNITxB5_xmr8oUbld2XvdrEtLtxhXR3Ac1WX87Cew",
// 		"contactName": "Nika David",
// 		"contactPhoto": "http://localhost:8000/storage/uploads/profiles/1737732826.png",
// 		"contactId": "dm2EDdEy5Y",
// 		"senderId": "mWMoJME4Qj",
// 		"messages": [
// 			{
// 				"from": "me",
// 				"body": "<img src=\"http://localhost:8000/assets/lib/jquery-emojiarea/jquery-emojiarea/packs/custom/beaming_face_with_smiling_eyes.png\" alt=\":beaming_face_with_smiling_eyes:\" class=\"emoji-img\"> Hey girl i'm waiting on yah. I'm waiting on yah, come one and let me sneak you out",
// 				"date": "Fri AT 12:04 PM Sep 26, 2025"
// 			},
// 			{
// 				"from": "contact",
// 				"body": "And have a celebration a celebration\r\nThe music up the windows down<img src=\"http://localhost:8000/assets/lib/jquery-emojiarea/jquery-emojiarea/packs/custom/purple_heart.png\" alt=\":purple_heart:\" class=\"emoji-img\"> <img src=\"http://localhost:8000/assets/lib/jquery-emojiarea/jquery-emojiarea/packs/custom/rolling_on_the_floor_laughing.png\" alt=\":rolling_on_the_floor_laughing:\" class=\"emoji-img\">",
// 				"date": ""
// 			},
// 			{
// 				"from": "me",
// 				"body": "Yeah we'll be doing what we do,\r\nJust pretending that we're cool and we know it too (know it too).\r\nYeah, we'll keep doing what we do\r\nJust pretending that we're cool so tonight...",
// 				"date": "Thu AT 12:13 PM Oct 02, 2025"
// 			},
// 			{
// 				"from": "contact",
// 				"body": "Let's go crazy, crazy, crazy 'til we see the sun\r\nI know we only met, but let's pretend it's love <img src=\"http://localhost:8000/assets/lib/jquery-emojiarea/jquery-emojiarea/packs/custom/blue_heart.png\" alt=\":blue_heart:\" class=\"emoji-img\">",
// 				"date": ""
// 			}
// 		]
// 	}
// };

let scrollContainer;

$(document).ready(() => {

    window.history.pushState(null, "", window.location.href);
    window.onpopstate = function () {
        history.pushState(null, "", window.location.href);
    };

    // renderDetails(data)

    var $wysiwyg = $('.chat-textarea').emojiarea({
        wysiwyg: true,
        button: '#btn-emoji-picker'
    });

	var $wysiwyg_value = $('#emojis-wysiwyg-value');
		
	$wysiwyg.on('change', function() {
		$wysiwyg_value.text($(this).val());
	});
	$wysiwyg.trigger('change');

    // $(document).on('input blur', '.emoji-wysiwyg-editor', function () {
    //     const $el = $(this);
    
    //     // Step 1: Get raw HTML and sanitize tags
    //     const dirtyHtml = $el.html();
    //     const cleanHtml = DOMPurify.sanitize(dirtyHtml, {
    //         ALLOWED_TAGS: ['img', 'div', 'h6', 'button'],
    //         ALLOWED_ATTR: ['src', 'alt', 'class']
    //     });
    
    //     // Step 2: Strip Unicode emojis from the sanitized HTML's text content
    //     const temp = $('<div>').html(cleanHtml);
    //     const strippedText = stripUnicodeEmojis(temp.text());
    
    //     // Step 3: Replace contenteditable with clean text only
    //     $el.html(strippedText);
    
    //     // Optional: Restore caret
    //     placeCaretAtEnd($el[0]);
    
    //     // Step 4: Update placeholder logic
    //     updatePlaceholder($el);
    // });   

    $(document).on('input blur', '.emoji-wysiwyg-editor', function () {
        const el = this;
    
        sanitizeEmojiEditor(el); // Clean text nodes only
        updatePlaceholder($(el)); // Still works
    });    
    

    const btnScrollDown = $('.btn-scroll-latest');
    scrollContainer = document.querySelector('.messenger-convbox');

    // scrollContainer.addEventListener('scroll', () => {
    //     const scrollTop     = scrollContainer.scrollTop;
    //     const scrollHeight  = scrollContainer.scrollHeight;
    //     const clientHeight  = scrollContainer.clientHeight;

    //     const scrollBottom  = scrollHeight - scrollTop - clientHeight;
    //     // const threshold     = scrollHeight * 0.25; // 2/8 = 25% <-- Threshold too high above
    //     const threshold     = scrollHeight * 0.06; // 2/30 = 6%

    //     if (scrollBottom > threshold)// && shouldShowScrollDownButton(scrollContainer))
    //     {
    //         // When user scrolls up show the "scroll down the latest message" button
    //         btnScrollDown.show();
    //     }
    //     else
    //     {
    //         btnScrollDown.hide();
    //     }
    // });

    scrollContainer.addEventListener('scroll', () => {
        const scrollTop     = scrollContainer.scrollTop;
        const scrollHeight  = scrollContainer.scrollHeight;
        const clientHeight  = scrollContainer.clientHeight;
    
        const scrollBottom  = scrollHeight - scrollTop - clientHeight;
        const threshold     = scrollHeight * 0.024;
    
        const isScrollable  = scrollHeight > clientHeight;
        const isScrolledUp  = scrollBottom > threshold;
    
        if (isScrollable && isScrolledUp) {
            btnScrollDown.show();
        } else {
            btnScrollDown.hide();
        }
    });    

    btnScrollDown.on('click', scrollConvoToBottom);

    window.addEventListener('resize', () => {
        scrollContainer.dispatchEvent(new Event('scroll'));
    });    
    
    // newMessageObserver.observe(scrollContainer, { childList: true, subtree: true });

});

function supportsUnicodePropertyEscapes() {
    try {
        new RegExp('\\p{Emoji_Presentation}', 'u');
        return true;
    } catch (e) {
        return false;
    }
}

// Remove system level emojies
function stripUnicodeEmojis(text) {
    if (supportsUnicodePropertyEscapes()) {
        return text.replace(/[\p{Emoji_Presentation}\p{Extended_Pictographic}\uFE0F\u200D]/gu, '');
    } else {
        return text.replace(
            /([\u2700-\u27BF]|[\uE000-\uF8FF]|[\uD800-\uDFFF]|\uFE0F|\u200D|[\u1F000-\u1FAFF])/g,
            ''
        );
    }
}

function sanitizeEmojiEditor(el) {
    const walker = document.createTreeWalker(el, NodeFilter.SHOW_TEXT, null, false);

    while (walker.nextNode()) {
        const node = walker.currentNode;
        const clean = stripUnicodeEmojis(node.textContent);

        // Also remove invisible fragments like ZWJ and variation selectors
        const cleaned = clean.replace(/[\u200D\uFE0F]/g, '');
            //.replace(/[\u200D\uFE0F]/g, '')
            //.replace(/\u00A0/g, ' '); // Normalize NBSP to regular space
    
        if (cleaned !== node.textContent) {
            node.textContent = cleaned;
        }
    }
}


function shouldShowScrollDownButton(scrollContainer)
{
    const scrollHeight = scrollContainer.scrollHeight;
    const clientHeight = scrollContainer.clientHeight;

    // Only show button if there's overflow (i.e., scrollable content)
    return scrollHeight > clientHeight;
}

function renderDetails(data)
{
    if (data === '' || data === undefined || data === null)
    {
        return;
    }

    // If the page is fully loaded, it closes the loader automatically during first load
    // Now if we want to do long running action, we can manually show the loader
    // then hide it manually later on
    if (IS_FULLY_LOADED)
        showLoading();

    try
    {
        const convbox = $('.messenger-convbox');

        let obj = (typeof data !== 'object' ? JSON.parse(data) : data).content;

        $('#header-contact-name').text(obj.contactName);
        $('#contact-photo').attr('src', obj.contactPhoto);

        if ('messages' in obj && obj.messages.length > 0)
        {
            // convbox.empty();

            // for (let i = 0; i < 20; i++)
            // {
                
            // }

            obj.messages.forEach(item => {

                if (item.date.trim() !== '')
                    convbox.append(`<div class="text-13 text-center text-uppercase my-3 opacity-70">${item.date}`);

                const rawHtml = `<div class="convo-item from-${item.from}">${item.body}</div>`;
                const html = DOMPurify.sanitize(rawHtml, {
                    ALLOWED_TAGS: ['img', 'div', 'h6', 'button'],
                    ALLOWED_ATTR: ['src', 'alt', 'class']
                });

                convbox.append(html);
            });
        }

        scrollConvoToBottom();

        // if (scrollContainer === undefined)
        //     scrollContainer = document.querySelector('.messenger-convbox');

        // scrollContainer.dispatchEvent(new Event('scroll'));
    }
    catch (e)
    {
        console.log(e)
        let err = 'Sorry, we encountered a technical issue and had to exit.';

        AlertWarn(err, 'Critical Error', {
            'onOK': function()
            {
                if (window.ChatJsBridge)
                    window.ChatJsBridge.onGoBack();
            }
        });
    }

    if (IS_FULLY_LOADED)
        hideLoading();
}

function scrollConvoToBottom()
{
    convbox = document.querySelector('.messenger-convbox');
    if (convbox) {
        convbox.scrollTop = convbox.scrollHeight;
    }
}
//
//=======================================//
//      Used when sending message        //
//=======================================//
//
function extractShortcodesFromHtml(html)
{
    const container = document.createElement('div');
    container.innerHTML = html;

    // Replace all <img> tags with their alt shortcodes
    const images = container.querySelectorAll('img');

    for (const img of images)
    {
        const shortcode = img.getAttribute('alt') || '';
        const textNode = document.createTextNode(shortcode);
        img.parentNode.replaceChild(textNode, img);
    }

    // Remove zero-width characters and trim
    return container.textContent.replace(/\u200B/g, '').trim();
}

function renderEmojis(text)
{
    const icons = $.emojiarea.icons;
    const basePath = $.emojiarea.path;

    const rawHtml = text.replace(/:\w+:/g, function (match) {
        if (icons[match]) {
            return `<img src="${basePath}/${icons[match]}" alt="${match}" class="emoji-img">`;
        }
        return match; // leave unknown shortcodes untouched
    });

    // Sanitize the final HTML before returning
    return DOMPurify.sanitize(rawHtml, {
        ALLOWED_TAGS: ['img', 'div', 'h6', 'button'],
        ALLOWED_ATTR: ['src', 'alt', 'class']
    });
}

function enqueueMessage(fromSender, message)
{
    const convbox = $('.messenger-convbox');

    let from = '';

    if (fromSender == 'contact')
        from = 'from-contact';

    else if (fromSender == 'me')
        from = 'from-me';

    // Step 1: Convert shortcodes to emoji <img> tags
    let emojiHtml = renderEmojis(message);

    // Step 2: Sanitize the rendered HTML
    let body = DOMPurify.sanitize(emojiHtml);

    // Step 3: Create the bubble
    let template = `<div class="convo-item ${from}">${body}</div>`;

    // Step 4: Append to chat log
    convbox.append(template);
}

function updatePlaceholder($el)
{
    const content = $el.text().trim();

    if (content === '')
        $el.addClass('is-empty');

    else
        $el.removeClass('is-empty');
}

function clearEditor()
{
    let editor = $('.emoji-wysiwyg-editor');

    // Clear content
    editor.html('');

    // Trigger input to re-run placeholder logic
    editor.trigger('input');
}