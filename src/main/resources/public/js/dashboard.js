$(document).ready(function() {
    resizeCommands();
    $('#save-channel-config').click(function() {
        $.ajax({
            url: '../../dash/channel',
            method: 'POST',
            data: {
                guildId: $('#guild-id').val(),
                'welcome-message-val': $('#welcome-message').val(),
                'welcome-channel-val': $('#welcome-channel option:selected').val(),
                'log-channel-val': $('#log-channel option:selected').val(),
                'twitch-channel-val': $('#twitch-channel option:selected').val(),
                'music-channel-val': $('#music-channel option:selected').val(),
                'music-voicechannel-val': $('#music-voicechannel option:selected').val()
            },
            success: function(res) {
                notify(res, 'success');
            },
            error: function(res) {
                notify(res.responseText, 'error');
            }
        });
    });
    $('#save-server-config').click(function() {
        $.ajax({
            url: '../../dash/server',
            method: 'POST',
            data: {
                guildId: $('#guild-id').val(),
                limitRoles: $('#role-limitation').is(':checked'),
                deleteInvites: $('#delete-invites').is(':checked'),
                slowMode: $('#slow-mode').val(),
                commandPrefix: $('#command-prefix').val(),
                djRole: $('#dj-role option:selected').val(),
                autoAssignRole: $('#auto-assign-role option:selected').val()
            },
            success: function(res) {
                notify(res, 'success');
            },
            error: function(res) {
                notify(res.responseText, 'error');
            }
        });
    });
    $("#feed-channels").change(function() {
        $.ajax({
            url: '../../api/reddit',
            method: 'GET',
            data: {
                channelId: $(this).val(),
                guildId: $('#guild-id').val()
            },
            success: function(res) {
                var json = JSON.parse(res);
                $('#subreddit-list').empty();
                for (var i = 0; i < json.length; i++) {
                    $('#subreddit-list').append($('<option/>', {
                        text: json[i]
                    }));
                }
                resizeSubreddit();
            }
        });
        $.ajax({
            url: '../../api/twitter',
            method: 'GET',
            data: {
                channelId: $(this).val(),
                guildId: $('#guild-id').val()
            },
            success: function(res) {
                var json = JSON.parse(res);
                $('#twitter-list').empty();
                for (var i = 0; i < json.length; i++) {
                    $('#twitter-list').append($('<option/>', {
                        text: json[i]
                    }));
                }
                resizeTwitter();
            }
        });
        $.ajax({
            url: '../../api/twitch',
            method: 'GET',
            data: {
                channelId: $(this).val(),
                guildId: $('#guild-id').val()
            },
            success: function(res) {
                var json = JSON.parse(res);
                $('#twitch-list').empty();
                for (var i = 0; i < json.length; i++) {
                    $('#twitch-list').append($('<option/>', {
                        text: json[i]
                    }));
                }
                resizeTwitch();
            }
        });
    });
    $('#add-twitter').webuiPopover({
        type: 'async',
        width: 300,
        dismissible: false,
        url: '../../etc/addtwitter.html'
    })
    $('#remove-twitter').click(function() {
        $.ajax({
            url: '../../api/twitter/remove',
            method: 'POST',
            data: {
                channelId: $('#feed-channels option:selected').val(),
                twitter: $('#twitter-list option:selected').text(),
                guildId: $('#guild-id').val()
            },
            success: function(res) {
                $('#twitter-list option:selected').remove();
                notify(res, 'success');
                resizeTwitter();
            },
            error: function(res) {
                notify(res.responseText, 'error');
            }
        });
    });
    $('#add-subreddit').webuiPopover({
        type: 'async',
        width: 300,
        dismissible: false,
        url: '../../etc/addsub.html'
    });
    $('#remove-subreddit').click(function() {
        $.ajax({
            url: '../../api/reddit/remove',
            method: 'POST',
            data: {
                channelId: $('#feed-channels option:selected').val(),
                subreddit: $('#subreddit-list option:selected').text(),
                guildId: $('#guild-id').val()
            },
            success: function(res) {
                $('#subreddit-list option:selected').remove();
                notify(res, 'success');
                resizeSubreddit();
            },
            error: function(res) {
                notify(res.responseText, 'error');
            }
        });
    });
    $('#add-twitch').webuiPopover({
        type: 'async',
        width: 300,
        dismissible: false,
        url: '../../etc/addtwitch.html'
    });
    $('#remove-twitch').click(function() {
        $.ajax({
            url: '../../api/twitch/remove',
            method: 'POST',
            data: {
                channelId: $('#feed-channels option:selected').val(),
                twitch: $('#twitch-list option:selected').text(),
                guildId: $('#guild-id').val()
            },
            success: function(res) {
                $('#twitch-list option:selected').remove();
                notify(res, 'success');
                resizeTwitch();
            },
            error: function(res) {
                notify(res.responseText, 'error');
            }
        });
    });
    $('#disable-command').click(function() {
        $.ajax({
            url: '../../api/commands/disable',
            method: 'POST',
            data: {
                command: JSON.stringify($('#enabled-list').val()),
                guildId: $('#guild-id').val()
            },
            success: function(res) {
                $('#enabled-list option:selected').remove().appendTo('#disabled-list');
                resizeCommands();
                notify(res, 'success');
            },
            error: function(res) {
                notify(res.responseText, 'error');
            }
        });
    });
    $('#enable-command').click(function() {
        $.ajax({
            url: '../../api/commands/enable',
            method: 'POST',
            data: {
                command: JSON.stringify($('#disabled-list').val()),
                guildId: $('#guild-id').val()
            },
            success: function(res) {
                $('#disabled-list option:selected').remove().appendTo('#enabled-list');
                resizeCommands();
                notify(res, 'success');
            },
            error: function(res) {
                notify(res.responseText, 'error');
            }
        });
    });
    $('body').on('click', '#add-subreddit-btn', function() {
        $.ajax({
            url: '../../api/reddit/add',
            method: 'POST',
            data: {
                channelId: $('#feed-channels option:selected').val(),
                subreddit: $('#subreddit-name').val(),
                showImages: $('#show-image').val(),
                showText: $('#show-text').val(),
                guildId: $('#guild-id').val()
            },
            success: function(res) {
                $('#subreddit-list').append($('<option/>', {
                    text: $('#subreddit-name').val()
                }));
                notify(res, 'success')
                resizeSubreddit();
                $('#show-image option:eq(0)').prop('selected', true);
                $('#show-text option:eq(0)').prop('selected', true);
                $('#subreddit-name').val('');

                $('#add-subreddit').webuiPopover('hide');
            },
            error: function(res) {
                notify(res.responseText, 'error');
            }
        })
    }).on('click', '#add-twitter-btn', function() {
        $.ajax({
            url: '../../api/twitter/add',
            method: 'POST',
            data: {
                channelId: $('#feed-channels option:selected').val(),
                twitter: $('#twitter-name').val(),
                showImages: $('#show-image-twitter').val(),
                showRetweets: $('#show-retweets-twitter').val(),
                showReplies: $('#show-replies-twitter').val(),
                guildId: $('#guild-id').val()
            },
            success: function(res) {
                $('#twitter-list').append($('<option/>', {
                    text: $('#twitter-name').val()
                }));
                notify(res, 'success')
                resizeTwitter();
                $('#show-image option:eq(0)').prop('selected', true);
                $('#show-retweets option:eq(0)').prop('selected, true');
                $('#show-replies option:eq(0)').prop('selected, true');
                $('#twitter-name').val('');

                $('#add-twitter').webuiPopover('hide');
            },
            error: function(res) {
                notify(res.responseText, 'error');
            }
        })
    }).on('click', '#add-twitch-btn', function() {
        $.ajax({
            url: '../../api/twitch/add',
            method: 'POST',
            data: {
                channelId: $('#feed-channels option:selected').val(),
                twitch: $('#twitch-name').val(),
                guildId: $('#guild-id').val()
            },
            success: function(res) {
                $('#twitch-list').append($('<option/>', {
                    text: $('#twitch-name').val()
                }));
                notify(res, 'success')
                resizeTwitter();
                $('#twitch-name').val('');

                $('#add-twitter').webuiPopover('hide');
            },
            error: function(res) {
                notify(res.responseText, 'error');
            }
        })
    });
});

function notify(message, type) {
    noty({
        text: message,
        type: type,
        layout: 'topRight',
        progressBar: true,
        theme: 'relax',
        timeout: 3000,
        animation: {
            open: {
                height: 'toggle'
            },
            close: {
                height: 'toggle'
            },
            easing: 'swing',
            speed: 500 // opening & closing animation speed
        }
    });
}

function resizeSubreddit() {
    $('#subreddit-list')
        .attr('size',
            $('#subreddit-list option').length == 0 ? 1 :
            $('#subreddit-list option').length);
}

function resizeTwitter() {
    $('#twitter-list')
        .attr('size',
            $('#twitter-list option').length == 0 ? 1 :
            $('#twitter-list option').length);
}

function resizeTwitch() {
    $('#twitch-list')
        .attr('size',
            $('#twitch-list option').length == 0 ? 1 :
            $('#twitch-list option').length);
}

function resizeCommands() {
    $('#enabled-list')
        .attr('size',
            $('#enabled-list option').length == 0 ? 1 :
            ($('#enabled-list option').length > 15 ? 15 : $('#enabled-list option').length));
    $('#disabled-list')
        .attr('size',
            $('#disabled-list option').length == 0 ? 1 :
            ($('#disabled-list option').length > 15 ? 15 : $('#disabled-list option').length));

    $("#enabled-list").html($("#enabled-list option").sort(function(a, b) {
        return a.text == b.text ? 0 : a.text < b.text ? -1 : 1
    }));
    $("#disabled-list").html($("#disabled-list option").sort(function(a, b) {
        return a.text == b.text ? 0 : a.text < b.text ? -1 : 1
    }));
}
