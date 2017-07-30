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
                    <h1 class="page-header"><#if userIcon??><img class="img-circle" src="${userIcon}" alt="" />&nbsp;</#if>${username}'s <strong>Servers</strong></h1>
                </div>
                <!-- /.col-lg-12 -->
            </div>
            <!-- /.row -->
            <div class="row">
                <div class="panel panel-info">
                    <div class="panel-heading">
                        <strong>Configurable servers</strong>
                    </div>
                    <div class="panel-body">
                      <blockquote>
          					<#list guilds as g>
          						<a class="guild-list" href="/dash/guild/${g.id}"><#if g.icon?length gt 0><img class="img-circle-small" src="${g.icon}" alt="">&nbsp;</#if>${g.name}</a></br>
          					</#list>
                      </blockquote>
                    </div>
                    <!-- /.panel-body -->
                </div>
                <!-- /.panel -->
            </div>
            <!-- /.row -->
        </div>
        <!-- /#page-wrapper -->

    </div>
    <!-- /#wrapper -->

    <!-- jQuery -->
    <script src="/vendor/jquery/jquery.min.js"></script>

    <!-- Bootstrap Core JavaScript -->
    <script src="/vendor/bootstrap/js/bootstrap.min.js"></script>

	<!-- Custom dashboard JavaScript -->
	<script src = "/js/dashboard.js"></script>

    <!-- Metis Menu Plugin JavaScript -->
    <script src="/vendor/metisMenu/metisMenu.min.js"></script>
    <!-- Custom Theme JavaScript -->
    <script src="/dist/js/sb-admin-2.min.js"></script>

</body>

</html>
