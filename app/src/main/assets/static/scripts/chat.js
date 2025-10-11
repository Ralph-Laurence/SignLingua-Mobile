let scrollContainer;
const STATE_DISABLED = 1;
const STATE_ENABLE = 0;

$(document).ready(() => {

    window.history.pushState(null, "", window.location.href);
    window.onpopstate = function () {
        history.pushState(null, "", window.location.href);
    };

    $('.btn-send').on('click', sendMessage);

    var $wysiwyg = $('.chat-textarea').emojiarea({
        wysiwyg: true,
        button: '#btn-emoji-picker'
    });

	var $wysiwyg_value = $('#emojis-wysiwyg-value');
		
	$wysiwyg.on('change', function() {
		$wysiwyg_value.text($(this).val());
	});
	$wysiwyg.trigger('change');

    $(document).on('input blur', '.emoji-wysiwyg-editor', function () {
        const el = this;
    
        sanitizeEmojiEditor(el); // Clean text nodes only
        updatePlaceholder($(el)); // Still works
    });    
    

    const btnScrollDown = $('.btn-scroll-latest');
    scrollContainer = document.querySelector('.messenger-convbox');

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

        // const scrollBottom  = scrollHeight - scrollTop - clientHeight;
        // const threshold     = scrollHeight * 0.25; // 2/8 = 25% <-- Threshold too high above
        // const threshold     = scrollHeight * 0.06; // 2/30 = 6%
    });    

    btnScrollDown.on('click', scrollConvoToBottom);

    window.addEventListener('resize', () => {
        scrollContainer.dispatchEvent(new Event('scroll'));
    });   
    
    $('#btn-call').on('click', function() {

        if (window.ChatJsBridge?.onInitiateCall)
            window.ChatJsBridge.onInitiateCall();
    });
});

function supportsUnicodePropertyEscapes()
{
    try
    {
        new RegExp('\\p{Emoji_Presentation}', 'u');
        return true;
    }
    catch (e)
    {
        return false;
    }
}

// Remove system level emojies
function stripUnicodeEmojis(text)
{
    if (supportsUnicodePropertyEscapes())
    {
        return text.replace(/[\p{Emoji_Presentation}\p{Extended_Pictographic}\uFE0F\u200D]/gu, '');
    }
    else
    {
        return text.replace(
            /([\u2700-\u27BF]|[\uE000-\uF8FF]|[\uD800-\uDFFF]|\uFE0F|\u200D|[\u1F000-\u1FAFF])/g,
            ''
        );
    }
}

function sanitizeEmojiEditor(el)
{
    const walker = document.createTreeWalker(el, NodeFilter.SHOW_TEXT, null, false);

    while (walker.nextNode())
    {
        const node = walker.currentNode;
        const clean = stripUnicodeEmojis(node.textContent);

        // Also remove invisible fragments like ZWJ and variation selectors
        const cleaned = clean.replace(/[\u200D\uFE0F]/g, '');
    
        if (cleaned !== node.textContent)
            node.textContent = cleaned;
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
    }
    catch (e)
    {
        notifyCriticalError();
    }

    if (IS_FULLY_LOADED)
        hideLoading();
}

function notifyCriticalError(errMsg = '')
{
    const fallbackMsg = 'Sorry, we encountered a technical issue and had to exit.';
    const err = errMsg.trim() !== '' ? errMsg : fallbackMsg;

    AlertWarn(err, 'Critical Error', {
        'onOK': function()
        {
            if (window.ChatJsBridge?.onGoBack)
                window.ChatJsBridge.onGoBack();
        }
    });

    /**
    If you want to be extra cautious (e.g., in case onGoBack exists but isn’t callable), you could do:

    if (typeof window.ChatJsBridge?.onGoBack === 'function') {
        window.ChatJsBridge.onGoBack();
    }
    */
}

function scrollConvoToBottom()
{
    convbox = document.querySelector('.messenger-convbox');

    if (convbox)
        convbox.scrollTop = convbox.scrollHeight;
}
//
//=======================================//
//      Used when sending message        //
//=======================================//
//
// function extractShortcodesFromHtml(html)
// {
//     const container = document.createElement('div');
//     container.innerHTML = html;

//     // Replace all <img> tags with their alt shortcodes
//     const images = container.querySelectorAll('img');

//     for (const img of images)
//     {
//         const shortcode = img.getAttribute('alt') || '';
//         const textNode = document.createTextNode(shortcode);
//         img.parentNode.replaceChild(textNode, img);
//     }

//     // Remove zero-width characters and trim
//     return container.textContent.replace(/\u200B/g, '').trim();
// }

function extractShortcodesFromHtml(html)
{
    const cleanHtml = DOMPurify.sanitize(html, {
        ALLOWED_TAGS: ['img', 'div', 'br', 'span'], // allow emoji containers
        ALLOWED_ATTR: ['alt', 'src', 'class']       // keep emoji metadata
    });

    const container = document.createElement('div');
    container.innerHTML = cleanHtml;

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

function enqueueMessage(payload)
{
    if (typeof payload !== 'string')
    {
        console.log("Invalid chat message payload received.");
        return;
    }

    const obj = JSON.parse(payload);
    const { fromSender, message } = obj;
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

    scrollConvoToBottom();
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

async function sendMessage()
{
    const rawHtml = document.querySelector('.emoji-wysiwyg-editor').innerHTML;
    let message = extractShortcodesFromHtml(rawHtml);
    
    if (message.length > 2000)
    {
        AlertWarn("Your message is too long. Try splitting it.", 'Send Failed');
        return;
    }    

    if (window.ChatJsBridge)
    {
        window.ChatJsBridge.onCaptureMessage(message);
        clearEditor();
    }
}

function setMessageControlsState(state)
{
    let sendBtn  = $('.btn-send');
    let callBtn  = $('#btn-call');
    let btnEmoji = $('#btn-emoji-picker');
    let editor   = $('.emoji-wysiwyg-editor');

    switch (state)
    {
        case STATE_ENABLE:
            
            // Throttle enabling...
            setTimeout(() => {
                callBtn.prop('disabled', false);
                sendBtn.prop('disabled', false);
                btnEmoji.prop('disabled', false);
                editor.prop('contenteditable', true);  
            }, 800);

            break;

        case STATE_DISABLED:
            callBtn.prop('disabled', true);
            sendBtn.prop('disabled', true);
            btnEmoji.prop('disabled', true);
            editor.prop('contenteditable', false);  
            break;
    }
}

function enableControls()
{
    setMessageControlsState(STATE_ENABLE)
}
function disableControls()
{
    setMessageControlsState(STATE_DISABLED)
}

function showIncomingCallRinger()
{
    $('#incoming-call-ringer').show();
}

function hideIncomingCallRinger()
{
    $('#incoming-call-ringer').hide();
}