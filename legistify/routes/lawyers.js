var express = require('express');
var router = express.Router();
var Lawyer = require('../models/lawyer');
var User = require('../models/user');
var jwt = require('jsonwebtoken');
var gcm = require('node-gcm');
config = require('../config');

/* GET users listing. */
router.get('/', function (req, res, next) {
    res.send('respond with a resource');
    next();
});

id = null;
adminToken = null;
superSecret = config.secret;

router.post('/login', function (req, res) {
    // find the user
    Lawyer.findOne
    ({
        name: req.body.name
    }, function (err, lawyer) {
        if (err) throw err;
        if (!lawyer) {
            res.json({success: false, message: 'Authentication failed. User not found.'});
        }
        else if (lawyer) {
            // check if password matches
            if (lawyer.password != req.body.password) {
                res.json({success: false, message: 'Authentication failed. Wrong password.'});
            }
            else {
                id = lawyer._id;
                // if user is found and password is right
                // create a token
                var token = jwt.sign(lawyer, superSecret, {
                    expiresIn: 60 * 60 * 24 // expires in 24 hours
                });
                // return the information including token as JSON
                res.json({
                    success: true,
                    token: token
                });
            }
        }

    });
});


router.post('/me', function (req, res) {
    // find the user
    Lawyer.findOne({
        name: req.body.name
    }, function (err, lawyer) {
        if (err) throw err;
        if (lawyer.name != 'Aditya') {
            res.json({success: false, message: 'Authentication failed. Admin not found.'});
        }
        else if (lawyer.name == 'Aditya') {
            // check if password matches
            if ('aditya' != req.body.password) {
                res.json({success: false, message: 'Authentication failed. Wrong password.'});
            }
            else {
                // if user is found and password is right
                // create a token
                adminToken = jwt.sign(lawyer, superSecret, {
                    expiresIn: 60 * 60 * 24 // expires in 24 hours
                });
                // return the information including token as JSON
                res.json({
                    success: true,
                    token: adminToken
                });
            }
        }

    });
});

router.post('/insertLawyer', function (req, res)
{
    var db = new Lawyer();
    var response = {};
    db.name = req.body.name;
    db.password = req.body.password;
    db.expertise = req.body.expertise;
    db.contact = req.body.contact;
    db.experience = req.body.experience;
    db.latitude = req.body.latitude;
    db.longitude = req.body.longitude;
    db.save(function (err) {
        if (err) {
            response = {"error": true, "message": "Error adding data"};
        } else {
            response = {"error": false, "message": "Data added"};
        }
        res.json(response);
    });
});

router.post('/nearbyLawyer', function (req, res)
{
    var response = {};
    var response2 = [];

    latitude = req.body.latitude;
    expertise = req.body.expertise;

    Lawyer.find({}, function (err, data) {
        if (err) {
            response = {"error": true, "message": "Error fetching data"};
        }

        else
        {
            i = 0;
            while(i < data.length)
            {
                if((((0 < (parseFloat(latitude) - parseFloat(data[i].latitude))) &&
                    ((parseFloat(latitude) - parseFloat(data[i].latitude)) < 0.002)) ||
                    ((0 < (parseFloat(data[i].latitude) - parseFloat(latitude))) &&
                    ((parseFloat(data[i].latitude) - parseFloat(latitude)) < 0.002))) &&
                    expertise.toString() === data[i].expertise)
                {
                    response = {"latitude": data[i].latitude, "longitude": data[i].longitude, "registerToken": data[i].registerToken};
                    response2.push(response);
                }

                i++;
            }
        }

        res.json(response2);
    });
});

router.post('/notifyLawyer', function (req, res)
{
    regToken = req.body.registerToken;
    userid = req.body.userid;
    
    var message = new gcm.Message();
    message.addData('userId', userid);
    
    var arr = regToken.toString().split(",");

    var sender = new gcm.Sender('AIzaSyA0dxqmbtPIb6xdCZzSZ4G5X7YaAfrmpfw');
    var regTokens = [];

    i = 0;
    while(i < arr.length) {
        regTokens.push(arr[i]);
        i++;
    }

    sender.send(message, { registrationTokens: regTokens }, function (err, response) {
        if(err)
            console.error(err);
        else res.json({});
    });
});

router.post('/checkUserStatus', function (req, res) {
    var response = {};
    user_id = req.body.id;
    User.findById(user_id, function (err, data) {
        if (err) {
            response = {"error": true, "message": "Error fetching data"};
        } else {
            response = {"status": data.status};
        }
        res.json(response);
    });
});

///////////////////////////// MIDDLEWARE START ////////////////////////////////////////////

// route middleware to verify a token
router.use(function (req, res, next) {
    // check header or url parameters or post parameters for token
    var token = req.body.token || req.query.token || req.headers['token'];
    // decode token
    if (token) {
        // verifies secret and checks exp
        jwt.verify(token, superSecret, function (err, decoded) {
            if (err) {
                return res.json({success: false, message: 'Failed to Authenticate token.'});
            } else {
                // if everything is good, save to request for use in other routes
                req.decoded = decoded;
                next();
            }
        });
    } else {
        // if there is no token
        // return an error
        return res.status(403).send({
            success: false,
            message: 'No token provided.'
        });
    }
});

///////////////////////////// MIDDLEWARE END ////////////////////////////////////////////

router.post('/findLawyer', function (req, res) {
    var response = {};
    Lawyer.findById(id, function (err, data) {
        if (err) {
            response = {"error": true, "message": "Error fetching data"};
        } else {
            response = {"error": false, "message": data};
        }
        res.json(response);
    });
});


module.exports = router;
