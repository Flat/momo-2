<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="A general-purpose Discord bot designed to streamline moderation while adding useful (and fun) functionality">
    <meta name="keywords" content="momo, discord, bot, moderation, administration, commands, useful">
    <title>${botName}</title>
    <link rel="icon" type="image/png" sizes="16x16" href="/favicon.png">
    <!-- Bootstrap Core CSS -->
    <link href="/vendor/bootstrap/css/bootstrap.min.css" rel="stylesheet">

    <!-- Custom CSS -->
    <link href="/dist/css/sb-admin-2.min.css" rel="stylesheet">

    <!-- 3rd party -->
    <link href="/dist/css/jquery.webui-popover.min.css" rel="stylesheet">
    <link href="/dist/css/bootstrap-toggle.min.css" rel="stylesheet">

    <!-- Custom Fonts -->
    <link href="/vendor/font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css">

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
        <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
        <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->
</head>

<body>

    <div id="wrapper">

        <!-- Navigation -->
        <nav class="navbar navbar-default navbar-static-top" role="navigation" style="margin-bottom: 0">
            <div class="navbar-header">
                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-brand" href="/">${botName} <strong>Dashboard</strong></a>
            </div>
            <!-- /.navbar-header -->
            <div class="navbar-default sidebar" role="navigation">
                <div class="sidebar-nav navbar-collapse">
                    <ul class="nav" id="side-menu">
                        <li>
                            <a href="/"><i class="fa fa-home fa-fw"></i> Home Page</a>
                        </li>
                        <li>
                            <a href="/commands"><i class="fa fa-terminal fa-fw"></i> Commands</a>
                        </li>
                        <li class="active">
                          <a href="/dash"><i class="fa fa-cog fa-fw"></i> Your Dashboard</a>
                        </li>
                        <li>
                            <a href="/status"><i class="fa fa-heartbeat fa-fw"></i> Bot Status</a>
                        </li>
                        <li>
                            <a href="/changelog"><i class="fa fa-archive fa-fw"></i> Changelog</a>
                        </li>
                    </ul>
                </div>
                <!-- /.sidebar-collapse -->
            </div>
            <!-- /.navbar-static-side -->
        </nav>

        <div id="page-wrapper">
            <div class="row">
                <div class="col-lg-12">
                    <h1 class="page-header"><#if serverIcon??><img class="img-circle" src="${serverIcon}" alt="" />&nbsp;</#if>Server <strong>Configuration</strong> <small>${serverName}</small></h1>
                </div>
                <input type="hidden" id="guild-id" value="${guildid}">
                <!-- /.col-lg-12 -->
            </div>
            <!-- /.row -->
            <#if permissions gte 3>
            <div class="row">
                <div class="panel panel-info">
                    <div class="panel-heading">
                        <strong>Server configuration</strong> <small>Oh boy</small>
                    </div>
                    <div class="panel-body">
                        <div class="row">
                            <div class="col-lg-2">
                              <div class="form-group">
                                <label>Limit users to one role with <code>joinablerole</code>?</label>
                                <input id="role-limitation" <#if joinableRoleLimitation>checked</#if> type="checkbox" data-toggle="toggle" data-width="175" data-on="Yes, limit them!" data-off="No, free-for-all">
                                <p class="help-block">Enable if you want accurate <code>$rolestats</code></p>
                              </div>
                            </div>

                            <div class="col-lg-2">
                              <div class="form-group">
                                <label>Automatically delete invite links?</label>
                                <input id="delete-invites" <#if deleteInvites>checked</#if> type="checkbox" data-toggle="toggle" data-width="175" data-on="Yes! Spam sucks" data-off="No, freedom!">
                                <p class="help-block">Does not affect users with kick+</p>
                              </div>
                            </div>

                            <div class="col-lg-2">
                              <div class="form-group">
                                <label>Message limit per 15 seconds</label>
                                <input id="slow-mode" maxlength="3" class="form-control" value="${slowMode}">
                                <p class="help-block">0 to disable. Does not affect users with kick+</p>
                              </div>
                            </div>

                            <div class="col-lg-2">
                              <div class="form-group">
                                <label>DJ Role</label>
                                <select class="form-control" name="dj-role-val" id="dj-role">
                                    <option value="none">none</option>
                                      <#list roles as r>
                                          <option <#if (djRole == r.getIdLong()?c)>selected</#if> value="${r.getIdLong()?c}">#${r.name}</option>
                                      </#list>
                                </select>
                                <p class="help-block">Restrict music functions to a role</p>
                              </div>
                            </div>

                            <div class="col-lg-2">
                              <div class="form-group">
                                <label>Auto Assign Role</label>
                                <select class="form-control" name="auto-assign-role-val" id="auto-assign-role">
                                    <option value="none">none</option>
                                      <#list roles as r>
                                          <option <#if (autoAssignRole == r.getIdLong()?c)>selected</#if> value="${r.getIdLong()?c}">#${r.name}</option>
                                      </#list>
                                </select>
                                <p class="help-block">Assign users a default role when they join</p>
                              </div>
                            </div>

                            <div class="col-lg-2">
                              <div class="form-group">
                                <label>Command prefix</label>
                                <input id="command-prefix" maxlength="6" class="form-control" value="${commandPrefix}">
                                <p class="help-block">Max length of 6 characters, no spaces</p>
                              </div>
                            </div>
                        </div>
                        <div class="row col-lg-3">
                            <button id="save-server-config" class="btn btn-default">Save changes</button>
                        </div>
                    </div>
                    <!-- /.panel-body -->
                </div>
                <!-- /.panel -->
            </div>
            <!-- /.row -->
            </#if>
            <#if permissions gte 3>
            <div class="row">
                <div class="panel panel-info">
                    <div class="panel-heading">
                        <strong>Channel configuration</strong> <small>Where to send my messages</small>
                    </div>
                    <div class="panel-body">
                            <div class="row">
                                <div class="col-lg-2">
                                    <div class="form-group">
                                        <label>Welcome Message</label>
                                        <input id="welcome-message" name="welcome-message-val" class="form-control" value="${welcomeMessage}">
                                        <p class="help-block">Leave blank for no message.<br/>Note: Use $user$ and $server$ to replace with a @mention and your Server name respectively</p>
                                    </div>
                                </div>
                                <div class="col-lg-2">
                                    <div class="form-group">
                                        <label>Welcome Channel</label>
                                        <select class="form-control" name="welcome-channel-val" id="welcome-channel">
                                                  <option value="none">none</option>
                                                  <option <#if pmWelcome>selected</#if> value="pmWelcome">Private Message</option>
                                                <#list channels as ch>
                                                  <option <#if welcome == ch.getIdLong()?c && !pmWelcome>selected</#if> value="${ch.getIdLong()?c}">#${ch.name}</option>
                                                </#list>
                                            </select>
                                        <p class="help-block">Choose your welcome channel</p>
                                    </div>
                                </div>
                                <div class="col-lg-2">
                                    <div class="form-group">
                                        <label>Log Channel</label>
                                        <select class="form-control" name="log-channel-val" id="log-channel">
                                            <option value="none">none</option>
                                          <#list channels as ch>
                                            <option <#if log == ch.getIdLong()?c>selected</#if> value="${ch.getIdLong()?c}">#${ch.name}</option>
                                          </#list>
                                      </select>
                                        <p class="help-block">Choose your moderation log channel</p>
                                    </div>
                                </div>
                                <div class="col-lg-2">
                                    <div class="form-group">
                                        <label>Music Text Channel</label>
                                        <select class="form-control" name="music-channel-val" id="music-channel">
                                            <option value="none">none</option>
                                          <#list channels as ch>
                                            <option <#if music == ch.getIdLong()?c>selected</#if> value="${ch.getIdLong()?c}">#${ch.name}</option>
                                          </#list>
                                      </select>
                                        <p class="help-block">Choose your Music channel for song updates</p>
                                    </div>
                                </div>
                                <div class="col-lg-2">
                                    <div class="form-group">
                                        <label>Music Channel</label>
                                        <select class="form-control" name="music-voicechannel-val" id="music-voicechannel">
                                            <option value="none">none</option>
                                          <#list voiceChannels as ch>
                                            <option <#if musicVoice == ch.getIdLong()?c>selected</#if> value="${ch.getIdLong()?c}">#${ch.name}</option>
                                          </#list>
                                      </select>
                                        <p class="help-block">Choose your voice channel for music</p>
                                    </div>
                                </div>
                            </div>
                            <!-- /.row (nested) -->
                            <div class="row col-lg-3">
                                <button id="save-channel-config" class="btn btn-default">Save changes</button>
                            </div>
                    </div>
                    <!-- /.panel-body -->
                </div>
                <!-- /.panel -->
            </div>
            <!-- /.row -->
            </#if>
            <#if permissions gte 1>
            <div class="row">
                <div class="panel panel-info">
                    <div class="panel-heading">
                        <strong>Feed configuration</strong> <small>Customize Reddit, Twitter, and Twitch.tv feeds for your server</small>
                    </div>
                    <div class="panel-body">
                        <form role="form">
                            <div class="row">
                                <div class="col-lg-3">
                                    <div class="form-group input-group">
                                        <label class="input-group-addon">Channels</label>
                                        <select class="form-control" id="feed-channels">
                                          <option selected value="">Select channel</option>
                                          <#list channels as ch>
                                          <option value="${ch.getIdLong()?c}">#${ch.name}</option>
                                          </#list>
                                        </select>
                                    </div>
                                </div>
                                <div class="col-lg-3">
                                    <div class="form-group input-group">
                                        <label class="input-group-addon">subreddit</label>
                                        <select size="1" id="subreddit-list" class="form-control" multiple>

                                        </select>
                                        <span id="add-subreddit" class="input-group-addon success"><i class="fa fa-plus" aria-hidden="true"></i></span>
                                        <span id="remove-subreddit" class="input-group-addon danger"><i class="fa fa-minus" aria-hidden="true"></i></span>
                                    </div>
                                </div>

                                <div class="col-lg-3">
                                    <div class="form-group input-group">
                                        <label class="input-group-addon">twitter</label>
                                        <select size="1" id="twitter-list" class="form-control" multiple>

                                        </select>
                                        <span id="add-twitter" class="input-group-addon success"><i class="fa fa-plus" aria-hidden="true"></i></span>
                                        <span id="remove-twitter" class="input-group-addon danger"><i class="fa fa-minus" aria-hidden="true"></i></span>
                                    </div>
                                </div>

                                <div class="col-lg-3">
                                    <div class="form-group input-group">
                                        <label class="input-group-addon">twitch.tv</label>
                                        <select size="1" id="twitch-list" class="form-control" multiple>

                                        </select>
                                        <span id="add-twitch" class="input-group-addon success"><i class="fa fa-plus" aria-hidden="true"></i></span>
                                        <span id="remove-twitch" class="input-group-addon danger"><i class="fa fa-minus" aria-hidden="true"></i></span>
                                    </div>
                                </div>
                            </div>
                        </form>
                    </div>
                    <!-- /.panel-body -->
                </div>
                <!-- /.panel -->
            </div>
            <!-- /.row -->
            </#if>
            <#if permissions gte 2>
            <div class="row">
                <div class="panel panel-info">
                    <div class="panel-heading">
                        <strong>Command configuration</strong> <small>Manage them! Note: Doesn't affect users with kick+. Multi-select with ctrl &amp; shift keys</small>
                    </div>
                    <div class="panel-body">
                        <form role="form">
                            <div class="row">
                                <div class="col-lg-3">
                                  <div class="form-group input-group">
                                    <label class="input-group-addon">Enabled</label>
                                    <select multiple class="form-control" id="enabled-list">
                                      <#list enabledCommands as c>
                                      <option value="${c}">${c}</option>
                                      </#list>
                                    </select>
                                    <span id="disable-command" class="input-group-addon danger"><i class="fa fa-arrow-right" aria-hidden="true"></i></span>
                                  </div>
                                </div>
                                <div class="col-lg-3">
                                  <div class="form-group input-group">
                                    <span id="enable-command" class="input-group-addon success"><i class="fa fa-arrow-left" aria-hidden="true"></i></span>
                                    <select multiple class="form-control" id="disabled-list">
                                      <#list disabledCommands as c>
                                      <option value="${c}">${c}</option>
                                      </#list>
                                    </select>
                                    <label class="input-group-addon">Disabled</label>
                                  </div>
                                </div>
                            </div>
                        </form>
                    </div>
                    <!-- /.panel-body -->
                </div>
                <!-- /.panel -->
            </div>
            <!-- /.row -->
            </#if>
        </div>
        <!-- /#page-wrapper -->

    </div>
    <!-- /#wrapper -->

    <!-- jQuery -->
    <script src="/vendor/jquery/jquery.min.js"></script>

    <!-- Bootstrap Core JavaScript -->
    <script src="/vendor/bootstrap/js/bootstrap.min.js"></script>

    <!-- Custom dashboard JavaScript -->
    <script src="/js/dashboard.js"></script>

    <!-- Featherlight -->
    <script src="/dist/js/jquery.webui-popover.min.js"></script>

    <!-- noty -->
    <script src="/dist/js/jquery.noty.packaged.min.js"></script>

    <!-- bootstrap toggle -->
    <script src="/dist/js/bootstrap-toggle.min.js"></script>

    <!-- Metis Menu Plugin JavaScript -->
    <script src="/vendor/metisMenu/metisMenu.min.js"></script>
    <!-- Custom Theme JavaScript -->
    <script src="/dist/js/sb-admin-2.min.js"></script>

</body>

</html>
