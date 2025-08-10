function updateRibbon(json)
{
    if (!json)
        return;

    let obj = JSON.parse(json);

    $('#dynamic-tutor-username').text(obj.username);
    $('#dynamic-tutor-photo').attr('src', obj.photo);
}