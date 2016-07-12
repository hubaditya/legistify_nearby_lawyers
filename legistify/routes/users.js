var express = require('express');
var router = express.Router();
var User = require('../models/user');
var jwt = require('jsonwebtoken');
config = require('../config');

/* GET users listing. */
router.get('/', function (req, res, next) {
  res.send('respond with a resource');
  next();
});

id = null;
superSecret = config.secret;

router.post('/insertUser', function (req, res) {
  var db = new User();
  var response = {};
  db.name = req.body.name;
  db.password = req.body.password;
  db.status = req.body.status;
  db.save(function (err) {
    if (err) {
      response = {"error": true, "message": "Error adding data"};
    } else {
      response = {"error": false, "message": "Data added"};
    }
    res.json(response);
  });
});

router.post('/login', function (req, res) {
  // find the user
  User.findOne({
    name: req.body.name
  }, function (err, user) {
    if (err) throw err;
    if (!user) {
      res.json({success: false, message: 'Authentication failed. User not found.'});
    }
    else if (user) {
      // check if password matches
      if (user.password != req.body.password) {
        res.json({success: false, message: 'Authentication failed. Wrong password.'});
      }
      else {
        id = user._id;
        // if user is found and password is right
        // create a token
        var token = jwt.sign(user, superSecret, {
          expiresIn: 60 * 60 * 24 // expires in 24 hours
        });
        // return the information including token as JSON
        res.json({
          "success": true,
          "token": token,
          "id": id
        });
      }
    }

  });
});

router.post('/updateUserStatus', function (req, res)
{
  var response = {};
  user_id = req.body.id;
  User.findById(user_id, function (err, data) {
    if (err) {
      response = {"error": true, "message": "Error fetching data"};
    } else {
      data.status = req.body.status;
      data.save(function (err) {
        if (err) {
          response = {"error": true, "message": "Error updating data"};
        } else {
          response = {"error": false, "message": "Data is updated"};
        }
        res.json(response);
      })
    }
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

router.post('/findUser', function (req, res) {
  var response = {};
  User.findById(id, function (err, data) {
    if (err) {
      response = {"error": true, "message": "Error fetching data"};
    } else {
      response = {"error": false, "message": data};
    }
    res.json(response);
  });
});

module.exports = router;
