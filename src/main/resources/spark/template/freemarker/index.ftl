<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="A general-purpose Discord bot designed with the user in mind. Featuring music, moderation commands, and per-server configuration, ${botName} does a lot for your Discord community.">
    <meta name="keywords" content="momo, discord, bot, moderation, administration, commands, useful">
    <meta property="og:image" content="etc/avi.png">
    <meta property="og:type" content="website">
    <meta property="og:description" content="A general-purpose Discord bot designed with the user in mind. Featuring music, moderation commands, and per-server configuration, ${botName} does a lot for your Discord community.">
    <meta property="og:url" content="https://momobot.io/">
    <title>${botName}</title>
    <link rel="icon" type="image/png" sizes="16x16" href="favicon.png">
    <!-- Bootstrap Core CSS -->
    <link href="../vendor/bootstrap/css/bootstrap.min.css" rel="stylesheet">

    <!-- Custom CSS -->
    <link href="../dist/css/sb-admin-2.min.css" rel="stylesheet">

    <!-- Custom Fonts -->
    <link href="../vendor/font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css">

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
                            <a href="commands"><i class="fa fa-terminal fa-fw"></i> Commands</a>
                        </li>
                        <li>
                            <a href="/dash"><i class="fa fa-cog fa-fw"></i> Your Dashboard</a>
                        </li>
                        <li>
                            <a href="status"><i class="fa fa-heartbeat fa-fw"></i> Bot Status</a>
                        </li>
                        <li>
                            <a href="changelog"><i class="fa fa-archive fa-fw"></i> Changelog</a>
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
                    <h1 class="page-header"><strong>${botName}</strong> <small>Your new discord bot</small></h1>
                </div>
                <!-- /.col-lg-12 -->
            </div>
            <!-- /.row -->
            <div class="row">
                <div class="col-lg-4">
                    <div class="panel panel-warning">
                        <div class="panel-heading">
                            <strong>Add to your server</strong>
                        </div>
                        <div class="panel-body">
                            <p>Make sure you have the <em>manage server</em> permission</p>
                            <p>Visit this link: <a href="http://momobot.io/invite">momobot.io/invite</a></p>
                        </div>
                    </div>
                </div>
                <!-- /.col-lg-4 -->
                <div class="col-lg-4">
                    <div class="panel panel-warning">
                        <div class="panel-heading">
                            <strong>Join the bot's discord server</strong>
                        </div>
                        <div class="panel-body">
                            <p>Need help? Have suggestions? Join my server and look for me</p>
                            <p>Join at this link: <a href="http://momobot.io/join">momobot.io/join</a></p>
                        </div>
                    </div>
                </div>
                <!-- /.col-lg-4 -->
                <div class="col-lg-4">
                    <div class="panel panel-warning">
                        <div class="panel-heading">
                            <strong>View Github repository</strong>
                        </div>
                        <div class="panel-body">
                            <p>Visit the Github repository to view the source or get build instructions</p>
                            <p>Visit this link: <a href="http://momobot.io/github">momobot.io/github</a></p>
                        </div>
                    </div>
                </div>
                <!-- /.col-lg-4 -->
            </div>
            <!-- /.row -->
            <div class="row">
                <div class="col-lg-12">
                    <div class="panel panel-info">
                        <div class="panel-heading">
                            <h4><strong>New feature: Web configuration!</strong></h4>
                        </div>
                        <div class="panel-body">
                            <p>At long last, you can now configure various aspects of your server through the comfort of your browser!</p>
                            <p>To get started, head on over to <a href="/dash">this link</a> or click on <strong>Your Dashboard</strong> on the left. Then, login with your Discord account. After that, well... It just works</p>
                            <p>The levels of access you get are <strong>tied to your permissions in that server</strong>. This gives everyone a chance to use the web interface if needed, rather than just the owner</p>
                        </div>
                    </div>
                </div>
            </div>
            <!-- v1.2 -->
            <div class="row">
                <div class="col-lg-12">
                    <div class="well">
                        <h3><strong>What is Momo?</strong></h3>
                        <p>
                            Momo is a general-purpose bot, helping you streamline mod actions as well as adding a bit more fun to your server. The bot comes with a plethora of commands and customization, from playing music in a voice-channel to looking up Final Fantasy XIV characters.
                        </p>
                        <p>Momo is 100% open-source and is based off of the <a href="https://github.com/DV8FromTheWorld/JDA">JDA</a> wrapper. If you're looking to host your own version of Momo or want to take a look at the code, feel free to check
                            out the <a href="https://github.com/paul-io/momo-2">repository</a></p>
                    </div>
                </div>
            </div>

        </div>
        <!-- /#page-wrapper -->

    </div>
    <!-- /#wrapper -->

    <!-- jQuery -->
    <script src="../vendor/jquery/jquery.min.js"></script>

    <!-- Bootstrap Core JavaScript -->
    <script src="../vendor/bootstrap/js/bootstrap.min.js"></script>

    <!-- Metis Menu Plugin JavaScript -->
    <script src="../vendor/metisMenu/metisMenu.min.js"></script>
    <!-- Custom Theme JavaScript -->
    <script src="../dist/js/sb-admin-2.min.js"></script>

</body>

</html>
