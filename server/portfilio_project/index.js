/**
 * [코드 실행]
 * $ npm init
 * $ npm install --save express
 * $ npm install body-parser
 * $ npm install mysql2 --save
 * $ npm install nodemailer nodemailer-smtp-pool --save
 * $ npm install ejs
 * $ npm install --save multer
 */


const express = require('express');
const app = express();
const bodyParser = require('body-parser');
const mysql      = require('mysql2');
const dbconfig   = require('./config/database.js');
const connection = mysql.createConnection(dbconfig);
const nodemailer = require('nodemailer');
const multer  = require('multer');
const fs = require('fs');
const crypto = require('crypto');
const path = require('path');

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(express.static('public'));

// configuration =========================
app.set('view engine', 'ejs');
app.set('views', './views');
app.set('port', process.env.PORT || 3000);


// [회원가입 화면] Android 로부터 이메일 인증 요청을 받음.
app.post("/EmailAuth", (req, res) => {
    var headers = req.headers.apikey;
    var response = {
        'status' : 200,
        'message' : '',
        'data' : '',
        'return_type': ''
    };
    
    // header (apiKey )
    if(headers != "from_android_app")
    {
        response.status = 400;
        response.message = "ApiKey is wrong. please check to your \'apiKey\'";

        res.json(response);
    }
    else 
    {
        let get_email = req.body.user_email;
        let sql = 'SELECT * FROM user WHERE email=?';
        let params = [get_email];
        connection.query(sql, params, (error, rows, fields) => {

            if(error){
                console.log(error);
            }

            // 전송받은 email 이 user table 에 있는 경우
            if(rows.length > 0) {
                res.json({
                    "success":true,
                    "message":"duplicate email"
                });
            }
            // 전송받은 email 이 user table 에 없는 경우
            else {
            
                /**
                * [Process]
                * 먼저, 토큰을 생성해서 전송받은 이메일로 "이메일 주소, 생성된 토큰"이 포함된 URL 주소를 전송한다.
                * 이메일 전송 성공 시, user 테이블에 "이메일 주소, 생성된 토큰, 토큰 인증 유무" 정보를 저장한다.
                * 저장하는 이유는, 이메일 인증 요청 후 자신의 이메일로 전송된 URL 에 "이메일 주소와 생성된 토큰"이 포함되고,
                * 인증 URL 클릭 시, user 테이블에 저장된 해당 이메일의 토큰을 서로 비교해야 하기 때문이다.
                */


                // [토큰 생성]
                // 보안을 더 강화하기 위해 get_email 앞에 "hongpil"을 붙여서 암호화해 token을 생성한다.
                const crypto_email = "hongpil"+get_email        
                const crypto_token = crypto.createHash('sha512').update(crypto_email).digest('hex');
                
                // nodemailer 모듈을 이용해 이메일 전송
                let transporter = nodemailer.createTransport({
                    service: 'gmail',
                    auth: {
                    user: 'hpk4981@gmail.com',  // gmail 계정 아이디를 입력
                    pass: 'amigotime1#'         // gmail 계정의 비밀번호를 입력
                    }
                });

                let mailOptions = {
                    from: 'hpk4981@gmail.com',
                    to: get_email,
                    subject: 'Hello, this is ProgrammerShop. Please verify your email.',
                    html: '<p>Please click the link below.</p>' +
                        "<a href='http://homeserverpc.iptime.org:3000/verify?email=" + get_email +"&token=" + crypto_token+"'>Authenticate</a>"
                };

                transporter.sendMail(mailOptions, function(error, info){
                    if (error) 
                    {
                        console.log(error);
                    }
                    else 
                    {
                        sql = 'INSERT INTO user (email, crypto_token, certify_status) \
                        VALUES(?, ?, ?)';
                        params = [get_email, crypto_token, false];
                        connection.query(sql, params, (error, rows, fields) => {

                            if(error) {
                                console.log(error);
                            }
                            else {
                                res.json({
                                    "success":true,
                                    "message":"email sending success"
                                });
                            }
                        });
                    }
                });
            }
        });
    }
});

// [회원가입 화면] 이메일로 전송된 "인증 링크" 클릭하면 여기로 들어옴.
app.get('/verify', (req, res) => {
    
    let get_email = req.query.email;
    let get_token = req.query.token;

    let sql = 'SELECT email,crypto_token,certify_status FROM user WHERE email=?';
    let params = [get_email];
    connection.query(sql, params, (error, rows, fields) => {

        if(error){
            console.log(error);
        } else {

            for(var i=0; i<rows.length; i++){
                if(get_token == rows[i].crypto_token) {
                    sql = 'UPDATE user SET certify_status=? WHERE email=?';
                    params = [true, rows[i].email];
                    connection.query(sql, params, (error, rows, fields) => {
                        if(error) {
                            console.log(error);
                        }
                        else {
                            res.render('after_mail_cert');
                        }
                    });

                }
                else {
                    console.log("=============");
                    console.log("토큰 일치하지 않음 ! 해당 email의 인증 상태 확인할 것 !");
                    console.log("user email : " + rows[i].email);
                    console.log("user token : " + rows[i].crypto_token);
                    console.log("=============");
                }
            }
        }
    });

});


// [회원가입 화면] Android 로부터 회원가입을 요청 받음.
app.post("/UserJoin", (req, res) => {
    var headers = req.headers.apikey;
    var response = {
        'status' : 200,
        'message' : '',
        'data' : '',
        'return_type': ''
    };

    if(headers != "from_android_app")
    {
        response.status = 400;
        response.message = "ApiKey is wrong. please check to your \'apiKey\'";

        res.json(response);
    }
    else 
    {
        let get_email = req.body.email;
        let get_password = req.body.password;
        let get_name = req.body.name;
        let changed_type_password = get_password.toString();
        let crypto_password = crypto.createHash('sha512').update(changed_type_password).digest('hex');  // 전송된 비밀번호 암호화

        let sql = 'SELECT certify_status FROM user WHERE email=?';
        let params = [get_email];
        connection.query(sql, params, (error, rows, fields) => {
            if(error){
                console.log(error);
            } else {

                for(var i=0; i<rows.length; i++){
                    if(rows[i].certify_status == 1) {
                        sql = 'UPDATE user SET name=?, password=? WHERE email=?';
                        params = [get_name, crypto_password, get_email];
                        connection.query(sql, params, (error, rows, fields) => {
                            if(error) {
                                console.log(error);
                            }
                            else {
                                res.json({
                                    "success":true,
                                    "message":"user added"
                                });
                            }
                        });

                    }
                    // 미인증
                    else {
                        res.json({
                            "success":true,
                            "message":"email auth not yet"
                        });
                    }
                }
            }
        });
    }
});


// [로그인 화면] Android 로부터 전송받은 사용자 계정(email/password) 정보가 Database에 있는지, 비밀번호는 맞는지 를 확인한 후, 결과를 return 한다.
app.post("/LoginUserCheck", (req, res) => {

    let get_email = req.body.login_email;
    let get_password = req.body.login_password;
    let changed_type_password = get_password.toString();
    let crypto_password = crypto.createHash('sha512').update(changed_type_password).digest('hex'); // changed_type_password를 암호화

    let sql = 'SELECT * FROM user WHERE email=?';
    let params = [get_email];
    connection.query(sql, params, (error, rows, fields) => {
        if(error){
            console.log(error);
        } else {
            if(rows.length > 0) {
                for(var i=0; i<rows.length; i++){
                    // 암호화된 비밀번호 확인 -> 로그인 성공
                    if(crypto_password == rows[i].password) {
            
                        let responseData = {};
                        responseData.token = rows[i].crypto_token;
                        responseData.userName = rows[i].name;
                        responseData.userId = rows[i].id;

                        res.json({
                            "success":true,
                            "message":"user check success",
                            "data":responseData
                        });
            
                    }
                    else {
                        res.json({
                            "success":true,
                            "message":"user check failed"
                        });
                    }
                }
            }
            // DB에 저장된 회원정보가 없는 경우
            else {
                res.json({
                    "success":true,
                    "message":"no user"
                });
            }
        }
    });
});

// 파일 업로드
let _storage = multer.diskStorage({
destination: 'uploads/',
    filename: function(req, file, cb) {
        return crypto.pseudoRandomBytes(16, function(err, raw) {
            if(err) {
                return cb(err);
            }
            return cb(null, file.originalname);
        });
    }
});

// 상품 등록
app.post('/ProductRegistration', 
multer({
    storage: _storage,
    limits: { fieldSize: 25 * 1024 * 1024 }
}).single('upload'), function (req, res) {
    try {
        let file = req.file;
        let originalName = '';
        let fileName = '';
        let mimeType = '';
        let size = 0;

        if(file) {
            originalName = file.originalname;
            filename = file.fileName;
            mimeType = file.mimetype;
            size = file.size;
            console.log("execute"+fileName);
        } else{ 
            console.log("request is null");
        }
    } catch (err) {
        console.dir(err.stack);
    }

    let userToken = req.headers.user_token;
    let userId = req.body.user_id;
    let productName = req.body.product_name;
    let productDescription = req.body.product_description;
    let productPrice = req.body.product_price;
    let productCategoryId = req.body.product_category_id;
    let productImagePath = req.file.path;

    let sql = 'SELECT crypto_token FROM user WHERE id=?';
    let params = [userId];
    connection.query(sql, params, (error, rows, fields) => {
        if(error){
            console.log(error);
        } else {

            for(var i=0; i<rows.length; i++){
                // 사용자 토큰 확인
                if(userToken == rows[i].crypto_token) {
                    sql = 'INSERT INTO product (category_id, description, name, price, user_id) \
                    VALUES(?, ?, ?, ?, ?)';
                    params = [productCategoryId, productDescription, productName, productPrice, userId];
                    connection.query(sql, params, (error, rows, fields) => {
                        if(error) {
                            console.log(error);
                        }
                        else {
                            let insertProductId = rows.insertId;

                            sql = 'INSERT INTO product_image (path, product_id) \
                            VALUES(?, ?)';
                            params = [productImagePath, insertProductId];
                            connection.query(sql, params, (error, rows, fields) => {
                                if(error) {
                                    console.log(error);
                                }
                                else {
                                    res.json({
                                        "success":true,
                                        "message":"product add success"
                                    });
                                }
                            });
                        }
                    });
                }
                // 토큰이 일치하지 않음
                else {
                    res.json({
                        "success":true,
                        "message":"인증 정보가 정확하지 않습니다."
                    });
                }
            }
        }
    });
});

// 카테고리별 모든 상품 조회
app.get('/AllProductsGet', function(req, res) {
    let userId = req.headers.user_id;
    let userToken = req.headers.user_token;
    let categoryId = req.query.category_id;
    let keyword = req.query.keyword;

    let sql = 'SELECT crypto_token FROM user WHERE id=?';
    let params = [userId];
    connection.query(sql, params, (error, rows, fields) => {
        if(error){
            console.log(error);
        } else {
            for(var i=0; i<rows.length; i++){
                // 사용자 토큰 확인
                if(userToken == rows[i].crypto_token) {
                    // '카테고리를 통한 상품 조회'인 경우
                    if(categoryId >= 0) {
                        sql = 'SELECT product.id, product.name, product.description, product.price, product.user_id, product_image.path \
                        FROM product JOIN product_image ON product.id = product_image.product_id \
                        WHERE product.category_id = ? \
                        ORDER BY product.updatedAT DESC;'
                        params = [categoryId];
                        connection.query(sql, params, (error, rows, fields) => {

                            if(error){
                                console.log(error);
                            } else {
                                res.json({
                                    "success":true,
                                    "message":"products get success",
                                    "data":rows
                                });
                            }
                        });
                    }
                    // '검색을 통한 상품 조회'인 경우
                    else {
                        let like_query = "%" + keyword + "%";
                        sql = 'SELECT product.id, product.name, product.description, product.price, product.user_id, product_image.path \
                        FROM product JOIN product_image ON product.id = product_image.product_id \
                        WHERE product.name LIKE ? \
                        ORDER BY product.updatedAT DESC;'
                        params = [like_query];
                        connection.query(sql, params, (error, rows, fields) => {
                            if(error){
                                console.log(error);
                            } else {
                                res.json({
                                    "success":true,
                                    "message":"products get success",
                                    "data":rows
                                });
                            }

                        });
                    }
                }
                // 토큰이 일치하지 않음
                else {
                    res.json({
                        "success":true,
                        "message":"인증 정보가 정확하지 않습니다."
                    });
                }
            }
        }
    });
});


// 선택된 상품 조회
app.get('/SingleProductGet', function(req, res) {
    let userId = req.headers.user_id;
    let userToken = req.headers.user_token;
    let productId = req.query.product_id;

    let sql = 'SELECT crypto_token FROM user WHERE id=?';
    let params = [userId];
    connection.query(sql, params, (error, rows, fields) => {
        if(error){
            console.log(error);
        } else {
            for(var i=0; i<rows.length; i++){
                // 사용자 토큰 확인
                if(userToken == rows[i].crypto_token) {
                    sql = 'SELECT product.id, product.name, product.description, product.price, product.user_id, product_image.path \
                    FROM product JOIN product_image ON product.id = product_image.product_id \
                    WHERE product.id = ? \
                    ORDER BY product.updatedAT DESC;'
                    params = [productId];
                    connection.query(sql, params, (error, rows, fields) => {
                        if(error){
                            console.log(error);
                        } else {
                            if(rows.length == 1) {
                                for(var i=0; i<rows.length; i++){
                                    res.json({
                                        "success":true,
                                        "message":"product get success",
                                        "data":{
                                            "id":rows[i].id,
                                            "name":rows[i].name,
                                            "description":rows[i].description,
                                            "price":rows[i].price,
                                            "user_id":rows[i].user_id,
                                            "path":rows[i].path,
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
                // 토큰이 일치하지 않음
                else {
                    res.json({
                        "success":true,
                        "message":"인증 정보가 정확하지 않습니다."
                    });

                }
            }
        }
    });
});


// 상품 업데이트
app.post('/ProductUpdate', 
multer({
    storage: _storage,
    limits: { fieldSize: 25 * 1024 * 1024 }
}).single('upload'), function (req, res) {

    try {

        let file = req.file;
        let originalName = '';
        let fileName = '';
        let mimeType = '';
        let size = 0;

        if(file) {
            originalName = file.originalname;
            filename = file.fileName;
            mimeType = file.mimetype;
            size = file.size;

            console.log("execute"+fileName);
            
        } else{ 
            console.log("request is null");
        }

    } catch (err) {

        console.dir(err.stack);
    }

    let userId = req.headers.user_id;
    let userToken = req.headers.user_token;
    let productId = req.body.product_id;
    let productName = req.body.product_name;
    let productDescription = req.body.product_description;
    let productPrice = req.body.product_price;
    let productImagePath = req.file.path;

    let sql = 'SELECT crypto_token FROM user WHERE id=?';
    let params = [userId];
    connection.query(sql, params, (error, rows, fields) => {
        if(error){
            console.log(error);
        } else {
            for(var i=0; i<rows.length; i++){
                // 토큰 일치
                if(userToken == rows[i].crypto_token) {
        
                    sql = 'UPDATE product SET description=?, name=?, price=? WHERE id=?';
                    params = [productDescription, productName, productPrice, productId];
                    connection.query(sql, params, (error, rows, fields) => {
                        if(error) {
                            console.log(error);
                        }
                        else {
                            sql = 'SELECT path FROM product_image WHERE product_id=?';
                            params = [productId];
                            connection.query(sql, params, (error, rows, fields) => {
                                if(error){
                                    console.log(error);
                                } else {
                                    for(var i=0; i<rows.length; i++){

                                        let tmp_image = rows[i].path;
                                        let new_tmp_image = tmp_image.replace(/uploads\//i, '');    // "uploads/" 제거
                                        const fs = require('fs');
                                        const path = require('path');
                                        let directory = 'uploads';

                                        fs.readdir(directory, (err, files) => {
                                            if (err) throw err;

                                            for (const file of files) {
                                                // 동일한 파일 이름이 있으면 삭제한다.
                                                if (new_tmp_image == file)
                                                {
                                                    fs.unlink(path.join(directory, new_tmp_image), err => {
                                                        console.log();
                                                        console.log(new_tmp_image + " 파일 삭제 완료 !")
                                                        console.log();
                                                    });
                                                }
                                            }
                                        });
                                    }
                                }
                            });

                            sql = 'UPDATE product_image SET path=? WHERE product_id=?';
                            params = [productImagePath, productId];
                            connection.query(sql, params, (error, rows, fields) => {
                                if(error) {
                                    console.log(error);
                                }
                                else {
                                    res.json({
                                        "success":true,
                                        "message":"product update success"
                                    });
                                }
                            });
                        }
                    });
                }
                else {
                    res.json({
                        "success":false,
                        "message":"인증 정보가 정확하지 않습니다."
                    });
                }
            }
        }
    });
});


// 상품 삭제
app.post('/ProductDelete', function(req, res) {

    let userId = req.headers.user_id;
    let userToken = req.headers.user_token;
    let productId = req.body.product_id;

    let sql = 'SELECT crypto_token FROM user WHERE id=?';
    let params = [userId];
    connection.query(sql, params, (error, rows, fields) => {
        if(error){
            console.log(error);
        } else {

            for(var i=0; i<rows.length; i++){
                // 사용자 토큰 확인
                if(userToken == rows[i].crypto_token) {
                    sql = 'DELETE FROM product WHERE id = ?'
                    params = [productId];
                    connection.query(sql, params, (error, result) => {
                        if(error){
                            console.log(error);
                        } else {
                            sql = 'DELETE FROM product_image WHERE product_id = ?'
                            params = [productId];
                            connection.query(sql, params, (error, result) => {
                                if(error){
                                    console.log(error);
                                } else {
                                    res.json({
                                        "success":true,
                                        "message":"product delete success"
                                    });
                                }
                            });
                        }
                    });
                }
                // 토큰이 일치하지 않음
                else {
                    res.json({
                        "success":true,
                        "message":"인증 정보가 정확하지 않습니다."
                    });
                }
            }
        }
    });
});

app.get('/uploads/:upload', function(req, res) {
let file = req.params.upload;
console.log(file);
let img = fs.readFileSync(__dirname + "/uploads/" + file);
res.writeHead(200, {'Content-Type': 'image/png'});
res.end(img, 'binary');
});


app.listen(app.get('port'), () => {
    console.log('Express server listening on port ' + app.get('port'));
});