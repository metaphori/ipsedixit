var USER, GAME, GAMEINFO={}, MSG;
$().ready(function(){

    $('form').on('submit', function(ev){
       ev.preventDefault();
       return false;        
    });

    $('#request_button').on('click', function(ev){
        game = $('#request input[name=match]').prop('value');
        user = $('#request input[name=user]').prop('value');
        USER = user;
        GAME = game;
        performAction("join", {user: user, match: game});
    });
    
    $('#phrasebtn').on('click', function(ev){
       performAction("setPhrase", {user: user, match: game, phrase: $('#cluetxt').val()});
       ev.preventDefault();
       return false;
    });
    
    $('#proceedbtn').on('click', function(ev){
       performAction("proceed", {user: user, match: game });
       $(this).css('display','none');
       ev.preventDefault();
       return false;        
    });
    
    // For select card, handlers are set when the cards are placed

});

function performAction(action, actionData){
    
    if(action==="join"){
        console.log("Joining the game...");
        data = createJoinXmlData(actionData.user, actionData.match);
        onsuccess = function(res){
            $('#request_button').css('display','none');
            $('#todo').text("Wait for the other players to join.");
            
            // The response must contain game status because we may simply need
            // to reconnect
            var gameinfo = res.documentElement.getElementsByTagName("GameInfo")[0];
            if (gameinfo) {
                syncWithGameInfo(gameinfo);
            }            
            
            performAction("pop", actionData);
        };
    } else if(action==="pop"){
        console.log("Now I wait for updates...");
        data = createGameUpdateXmlData(actionData.user, actionData.match);
        onsuccess = onGameUpdate;
    } else if(action==="setPhrase"){ 
        console.log("Setting phrase...");
        data = createPhraseXmlData(actionData.user, actionData.match, actionData.phrase);
    } else if(action==="selectCard"){
        console.log("Selecting card " + actionData.card);
        data = createSelectCardXmlData(actionData.user, actionData.match, actionData.card);
    } else if(action==="voteCard"){
        console.log("Voting card " + actionData.card);
        if(USER===GAMEINFO.turn){
            console.log("I cannot vote because it is my turn.");
            return;
        }
        data = createVoteCardXmlData(actionData.user, actionData.match, actionData.card);        
    } else if(action==="proceed"){
        console.log("Proceeding to next round");
        data = createBasicXmlData("ok", actionData.user, actionData.match);
        onsuccess = function(res){
            $('#request_button').css('display','none');
            $('#todo').text("Wait for the other players to do the same."); 
            
            var gameinfo = res.documentElement.getElementsByTagName("GameInfo")[0];
            if (gameinfo) {
                syncWithGameInfo(gameinfo);
            }            
             
            performAction("pop", actionData);
        };        
    }
    else {
        setError("Unknown action");
        return;
    }
    
    $.ajax({
            url: "/WebApplication/Dixit",
            type: "POST",
            processData: false,
            contentType: "text/xml; charset=utf-8",
            dataType: "xml",
            data: data,
            success: function(res){
                onsuccess(res);
            },
            error: function(err){
                console.log("Error: "+err);
            }
        });    
}

function onGameUpdate(res){
    console.log("Game update: " + res);
    
    if(res===null) return;
    
    docname = res.documentElement.tagName;
    
    if(docname==="Timeout"){
    }
    else if(docname==="Update"){
        MSG = res;
        console.log("Msg " + res.documentElement.tagName);
        var gameinfo = res.documentElement.getElementsByTagName("GameInfo")[0];
        if (gameinfo !== null) {
            syncWithGameInfo(gameinfo);
        }
    }
    else if(docname==="End"){
        return;
    }
    
    performAction("pop", {user: USER, match: GAME});
}

function createBasicXmlData(rootName, user, match){
    var data = document.implementation.createDocument("", "", null);
    var root = data.createElement(rootName);
    data.appendChild(root);
    var userelem = data.createElement("user");
    userelem.appendChild(data.createTextNode(user));
    var matchelem = data.createElement("match");
    matchelem.appendChild(data.createTextNode(match));
    root.appendChild(userelem);
    root.appendChild(matchelem);
    return data;    
}

function createJoinXmlData(user, match){
    return createBasicXmlData("join", user, match);
}

function createGameUpdateXmlData(user, match){
    return createBasicXmlData("pop", user, match);
}

function createPhraseXmlData(user, match, phrase){
    var data = createBasicXmlData("setPhrase", user, match);
    var phraseElem = data.createElement("phrase");
    phraseElem.appendChild(data.createTextNode(phrase));
    data.documentElement.appendChild(phraseElem);
    return data;
}

function createSelectCardXmlData(user, match, cardId){
    var data = createBasicXmlData("selectCard", user, match);
    var cardElem = data.createElement("card");
    cardElem.appendChild(data.createTextNode(cardId));
    data.documentElement.appendChild(cardElem);
    return data;    
}

function createVoteCardXmlData(user, match, cardId){
    var data = createBasicXmlData("voteCard", user, match);
    var cardElem = data.createElement("card");
    cardElem.appendChild(data.createTextNode(cardId));
    data.documentElement.appendChild(cardElem);
    return data;    
}

function syncWithGameInfo(gameinfo){
    GAMEINFO.players = [];
    GAMEINFO.your_cards = [];
    GAMEINFO.table_cards = [];
    
    console.log("Sync with game info.");
    var phase = gameinfo.getAttribute("phase");
    var turn = gameinfo.getAttribute("turn");
    var winner = gameinfo.getAttribute("winner");
    var phrase = gameinfo.getElementsByTagName("Phrase")[0];
    var players = gameinfo.getElementsByTagName("Player");
    var mycards = gameinfo.getElementsByTagName("YourCards")[0];
    var tablecards = gameinfo.getElementsByTagName("CardsOnTable")[0];
    var votes = gameinfo.getElementsByTagName("Votes")[0];
    var selection = gameinfo.getElementsByTagName("UncoveredCards")[0];
    
    GAMEINFO.phase = phase;
    GAMEINFO.turn = turn;
    GAMEINFO.votes = votes;
    
    if(mycards){
        setMyCards(mycards);   
    }
    if(tablecards){
        setTableCards(tablecards);
    }    
    if(phrase){
        $('#clue').text(phrase.textContent);
    }
    if(selection){
        uncoverCards(selection);
    }    
    if(votes){
        setVotes(votes);
    }
    
    $('#phasetxt').text(phase);
    $('#turntxt').text(turn);
    
    var disablePhrase = (turn===USER && phase==="setPhrase") ? null : 'true';
    $('#phrasebtn').prop('disabled', disablePhrase);
    $('#cluetxt').prop('disabled', disablePhrase);
    
    resultstbody = $("#resultsbody");
    resultstbody.html("");
    for (i = 0; i < players.length; i++) {
        playerName = players[i].textContent;
        points = players[i].getAttribute("points");
        resultstbody.append($("<tr><td>"+playerName+"</td><td>"+points+"</td></tr>"));
        console.log("I am with " + playerName);
        GAMEINFO.players[i] = { name: playerName, points: points };
    }
    
    if(phase==="setPhrase"){
        $('#tablecards').html('');
        if(turn===USER)
            $('#todo').text('Give the phrase, the clue for your card.');
        else
            $('#todo').text('Wait for ' + turn + ' to give the phrase.');
    }
    else if(phase==="selectCard")
        $('#todo').text('Now, select a card.');
    else if(phase==="vote"){
        if(turn===USER)
            $('#todo').text('Wait for the other players to vote.');
        else
            $('#todo').text('Vote a card');
    }
    else if(phase==="results"){
        $('#todo').text('Round finished.');
        $('#proceedbtn').css('display', 'inline-block');
    }
    else if(phase==="end")
        $('#todo').text("END OF MATCH. Winner is " + winner);
}

function uncoverCards(selectionElem){
    var sel = selectionElem.getElementsByTagName("Selection");
    for(i = 0; i<sel.length; i++){
        by = sel[i].getAttribute("by");
        to = sel[i].getAttribute("card");
        // TODO: ensure card names do not contain spaces or illegal chars wrt css selectors
        isTurn = USER===GAMEINFO.turn ? '*' : '';
        $('#tableImgDiv'+to).append("<p>BY: <u>"+by+isTurn+"</u></p>");
    }    
}

function setVotes(votesElem){
    var votes = votesElem.getElementsByTagName("Vote");
    for(i = 0; i<votes.length; i++){
        by = votes[i].getAttribute("by");
        to = votes[i].getAttribute("toCard");
        // TODO: ensure card names do not contain spaces or illegal chars wrt css selectors
        $('#tableImgDiv'+to).append("<p>VOTE: "+by+"</p>");
    }
}

function setTableCards(cards){
    console.log("Setting cards on table");
    var ccs = cards.getElementsByTagName("Card");
    table = $("#tablecards");
    table.html("");
    for(i = 0; i<ccs.length; i++){
        GAMEINFO.table_cards[i] = ccs[i].getAttribute("cardId");
        cardUrl = ccs[i].getAttribute("cardUrl");
        cardName = ccs[i].getAttribute("cardId");
        cardTitle = ccs[i].getAttribute("cardTitle");
        width = $("#playercards img").width();
        console.log("Width of cards on table " + width);
        height = width*3/2;
        img = $('<img src="' + cardUrl + '" name="' + cardName + 
                '" title="'+cardTitle+'" width="'+width+'" height="'+height+
                '" id="tableImg'+cardName+'"/>');
        imgcont = $('<div id="tableImgDiv'+cardName+'" class="dixitImgDiv">').wrapInner(img);
        if(USER!==GAMEINFO.turn){
            img.on('click', function(){
                $('#tablecards img').off('click');
                $(this).css('border', '5px solid #000');
                performAction('voteCard', {user: USER, match: GAME, card: $(this).attr('name')});
            });
        }
        table.append(imgcont);
    }    
}

function setMyCards(cards){
    console.log("Setting cards [user=" + USER + "; GAME="+GAME+"]");
    var ccs = cards.getElementsByTagName("Card");
    myhand = $("#playercards");
    myhand.html("");
    for(i = 0; i<ccs.length; i++){
        GAMEINFO.your_cards[i] = ccs[i].getAttribute("cardId");
        cardUrl = ccs[i].getAttribute("cardUrl");
        cardName = ccs[i].getAttribute("cardId");
        cardTitle = ccs[i].getAttribute("cardTitle");
        width = $("#playercards").width() / 5;
        height = width*3/2;
        img = $('<img src="' + cardUrl + '" name="' + cardName + 
                '" title="'+cardTitle+'" width="'+width+'" height="'+height+'" />');
        if(GAMEINFO.phase==="selectCard"){
            img.on('click', function(){ 
                $('#playercards img').off('click');
                $(this).css('border', '5px solid #000');
                performAction('selectCard', {user: USER, match: GAME, card: $(this).attr('name')});
            });
        }
        myhand.append(img);
    }
}

function setError(msg){
    console.log("ERROR: " + msg);
}

