var express = require('express');
var router = express.Router();
var Interact = require('../models/interaction');

/* GET users listing. */
router.get('/', function (req, res, next) {
    res.send('respond with a resource');
});

router.post('/insert', function (req, res) {
    var db = new Interact();
    var response = {};
    db.userid = req.body.userid;
    db.name = req.body.name;
    db.expertise = req.body.expertise;
    db.contact = req.body.contact;
    db.experience = req.body.experience;
    db.save(function (err) {
        if (err) {
            response = {"error": true, "message": "Error adding data"};
        } else {
            response = {"error": false, "message": "Data added"};
        }
        res.json(response);
    });
});

router.post('/findLawyerDetails', function (req, res) {
    var response = {};
    userid = req.body.userid;
    Interact.find({userid: userid}, function (err, data) {
        if (err) {
            response = {"error": true, "message": "Error fetching data"};
        } else {
            response = {"error": false, "message": data};
        }
        res.json(response);
    });
});

module.exports = router;


