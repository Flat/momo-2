<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="A general-purpose Discord bot designed to streamline moderation while adding useful (and fun) functionality">
    <meta name="keywords" content="momo, discord, bot, moderation, administration, commands, useful">
    <meta property="og:image" content="etc/avi.png">
    <meta property="og:type" content="website">
    <meta property="og:description" content="A general-purpose Discord bot designed with the user in mind. Featuring music, moderation commands, and per-server configuration, ${botName} does a lot for your Discord community.">
    <meta property="og:url" content="https://momobot.io/commands">
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
                <a class="navbar-brand" href="/">${botName} <strong>Dashboard</strong> </a>
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
                        <li class="active">
                            <a href="status"><i class="fa fa-heartbeat fa-fw"></i> Bot Status</a>
                        </li>
                        <li>
                          <a href="changelog" ><i class="fa fa-archive fa-fw"></i> Changelog</a>
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
                    <h1 class="page-header">Command <strong>Listing</strong> <small>Oh, the interaction!</small></h1>
                </div>
                <!-- /.col-lg-12 -->
            </div>
            <!-- /.row -->
            <div class="row">
                <div class="col-lg-12">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            Access: <strong>Everyone</strong>
                        </div>
                        <div class="panel-body">
                            <div class="table-responsive">
                                <table class="table table-striped">
                                    <thead>
                                        <tr>
                                            <th class="col-lg-2">Command</th>
                                            <th class="col-lg-1">Aliases</th>
                                            <th class="col-lg-6">Description</th>
                                            <th class="col-lg-3">Example</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <#list commandsEveryone as cmd>
                                            <tr>
                                            <td>${cmd.defaultCommand}</td>
                                            <td>${cmd.aliases?join(", ")}</td>
                                            <td>${cmd.description}</td>
                                            <td><code>$${cmd.defaultCommand} ${cmd.example?replace("\n", "</br>$${cmd.defaultCommand} ")}</code></td>
                                        </tr>
                                        </#list>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-lg-12">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            Access: <strong>Kick</strong>
                        </div>
                        <div class="panel-body">
                            <div class="table-responsive">
                                <table class="table table-striped">
                                    <thead>
                                        <tr>
                                            <th class="col-lg-2">Command</th>
                                            <th class="col-lg-1">Aliases</th>
                                            <th class="col-lg-6">Description</th>
                                            <th class="col-lg-3">Example</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <#list commandsKick as cmd>
                                            <tr>
                                            <td>${cmd.defaultCommand}</td>
                                            <td>${cmd.aliases?join(", ")}</td>
                                            <td>${cmd.description}</td>
                                            <td><code>$${cmd.defaultCommand} ${cmd.example?replace("\n", "</br>$${cmd.defaultCommand} ")}</code></td>
                                        </tr>
                                        </#list>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-lg-12">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            Access: <strong>Ban</strong>
                        </div>
                        <div class="panel-body">
                            <div class="table-responsive">
                                <table class="table table-striped">
                                    <thead>
                                        <tr>
                                            <th class="col-lg-2">Command</th>
                                            <th class="col-lg-1">Aliases</th>
                                            <th class="col-lg-6">Description</th>
                                            <th class="col-lg-3">Example</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <#list commandsBan as cmd>
                                            <tr>
                                            <td>${cmd.defaultCommand}</td>
                                            <td>${cmd.aliases?join(", ")}</td>
                                            <td>${cmd.description}</td>
                                            <td><code>$${cmd.defaultCommand} ${cmd.example?replace("\n", "</br>$${cmd.defaultCommand} ")}</code></td>
                                        </tr>
                                        </#list>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-lg-12">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            Access: <strong>Manage Roles</strong>
                        </div>
                        <div class="panel-body">
                            <div class="table-responsive">
                                <table class="table table-striped">
                                    <thead>
                                        <tr>
                                            <th class="col-lg-2">Command</th>
                                            <th class="col-lg-1">Aliases</th>
                                            <th class="col-lg-6">Description</th>
                                            <th class="col-lg-3">Example</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <#list commandsManageRoles as cmd>
                                            <tr>
                                            <td>${cmd.defaultCommand}</td>
                                            <td>${cmd.aliases?join(", ")}</td>
                                            <td>${cmd.description}</td>
                                            <td><code>$${cmd.defaultCommand} ${cmd.example?replace("\n", "</br>$${cmd.defaultCommand} ")}</code></td>
                                        </tr>
                                        </#list>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-lg-12">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            Access: <strong>Manage Server</strong>
                        </div>
                        <div class="panel-body">
                            <div class="table-responsive">
                                <table class="table table-striped">
                                    <thead>
                                        <tr>
                                            <th class="col-lg-2">Command</th>
                                            <th class="col-lg-1">Aliases</th>
                                            <th class="col-lg-6">Description</th>
                                            <th class="col-lg-3">Example</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <#list commandsManageServer as cmd>
                                            <tr>
                                            <td>${cmd.defaultCommand}</td>
                                            <td>${cmd.aliases?join(", ")}</td>
                                            <td>${cmd.description}</td>
                                            <td><code>$${cmd.defaultCommand} ${cmd.example?replace("\n", "</br>$${cmd.defaultCommand} ")}</code></td>
                                        </tr>
                                        </#list>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-lg-12">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            Access: <strong>Bot Owner</strong> <small>Note: The bot owner <em>does not</em> have access to any mod+ commands</small>
                        </div>
                        <div class="panel-body">
                            <div class="table-responsive">
                                <table class="table table-striped">
                                    <thead>
                                        <tr>
                                            <th class="col-lg-2">Command</th>
                                            <th class="col-lg-1">Aliases</th>
                                            <th class="col-lg-6">Description</th>
                                            <th class="col-lg-3">Example</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <#list commandsBotOwner as cmd>
                                            <tr>
                                            <td>${cmd.defaultCommand}</td>
                                            <td>${cmd.aliases?join(", ")}</td>
                                            <td>${cmd.description}</td>
                                            <td><code>$${cmd.defaultCommand} ${cmd.example?replace("\n", "</br>$${cmd.defaultCommand} ")}</code></td>
                                        </tr>
                                        </#list>
                                    </tbody>
                                </table>
                            </div>
                        </div>
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
