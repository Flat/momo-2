$(document).ready(function() {
    function drawCommands() {

        var msgPerFive = $("#messagesPerFive");
        var cmdPerFive = $("#commandsPerFive");
        msgPerFive.empty();

        var maximum = 30;


        var msgData = [];
        var cmdData = [];

        function request() {
            $.ajax({
                url: '../../public/counts?type=msg'
            }).done(function(res) {
                var arr = JSON.parse(res);
                msgData.push(arr[0]);
                cmdData.push(arr[1]);
            });
        }

        function getMsgData() {
            if (msgData.length >= maximum) {
                msgData = msgData.slice(1);
            }
            if (msgData.length == 0)
                for (i = 0; i < maximum; i++) {
                    msgData.push(-1);
                }
            request();

            var res = [];
            for (var i = 0; i < msgData.length; ++i) {
                res.push([i, msgData[i]])
            }
            return res;
        }

        function getCmdData() {
            if (cmdData.length >= maximum) {
                cmdData = cmdData.slice(1);
            }
            if (cmdData.length == 0)
                for (i = 0; i < maximum; i++) {
                    cmdData.push(-1);
                }

            var res = [];
            for (var i = 0; i < cmdData.length; ++i) {
                res.push([i, cmdData[i]])
            }
            return res;
        }

        msgSeries = [{
            data: getMsgData(),
            lines: {
                fill: true
            }
        }];
        cmdSeries = [{
            data: getCmdData(),
            lines: {
                fill: true
            }
        }];


        var msgPerFivePlot = $.plot(msgPerFive, msgSeries, {
            grid: {
                color: "#999999",
                tickColor: "#D4D4D4",
                borderWidth: 0,
                minBorderMargin: 20,
                labelMargin: 10,
                backgroundColor: {
                    colors: ["#ffffff", "#ffffff"]
                },
                margin: {
                    top: 8,
                    bottom: 20,
                    left: 20
                },
                markings: function(axes) {
                    var markings = [];
                    var xaxis = axes.xaxis;
                    for (var x = Math.floor(xaxis.min); x < xaxis.max; x += xaxis.tickSize * 2) {
                        markings.push({
                            xaxis: {
                                from: x,
                                to: x + xaxis.tickSize
                            },
                            color: "#fff"
                        });
                    }
                    return markings;
                }
            },
            xaxis: {
                tickFormatter: function() {
                    return "";
                }
            },
            yaxis: {
                min: 0,
                max: 100
            },
            legend: {
                show: true
            }
        });

        var cmdPerFivePlot = $.plot(commandsPerFive, cmdSeries, {
            grid: {
                color: "#999999",
                tickColor: "#D4D4D4",
                borderWidth: 0,
                minBorderMargin: 20,
                labelMargin: 10,
                backgroundColor: {
                    colors: ["#ffffff", "#ffffff"]
                },
                margin: {
                    top: 8,
                    bottom: 20,
                    left: 20
                },
                markings: function(axes) {
                    var markings = [];
                    var xaxis = axes.xaxis;
                    for (var x = Math.floor(xaxis.min); x < xaxis.max; x += xaxis.tickSize * 2) {
                        markings.push({
                            xaxis: {
                                from: x,
                                to: x + xaxis.tickSize
                            },
                            color: "#fff"
                        });
                    }
                    return markings;
                }
            },
            xaxis: {
                tickFormatter: function() {
                    return "";
                }
            },
            yaxis: {
                min: 0,
                max: 25
            },
            legend: {
                show: true
            }
        });
        setInterval(function updateRandom() {
            msgSeries[0].data = getMsgData();
            cmdSeries[0].data = getCmdData();
            msgPerFivePlot.setData(msgSeries);
            cmdPerFivePlot.setData(cmdSeries);
            msgPerFivePlot.draw();
            cmdPerFivePlot.draw();
        }, 2000);

    }

    function drawBotStats() {
        var users = 0;
        var connectedServers = 0;
        var memoryUsage = 0;
        var hour = 0;
        var minute = 0;

        function updateStats() {
            $.ajax({
                url: '../../public/counts?type=status'
            }).done(function(res) {
                var arr = JSON.parse(res);
                users = arr[0];
                memoryUsage = arr[1];
                connectedServers = arr[2];
                hour = arr[3];
                minute = arr[4];
            });
            $("#users").text(users);
            $("#connectedServers").text(connectedServers);
            $("#memoryUsage").text(memoryUsage);
            $("#hour").text(hour.zeroPad());
            $("#minute").text(minute.zeroPad());
            setTimeout(updateStats, 1000);
        }
        updateStats();
    }

    function getRandomInt(min, max) {
        return Math.floor(Math.random() * (max - min + 1)) + min;
    }
    drawBotStats();
    drawCommands();
});
