"use strict";var precacheConfig=[["/index.html","4eb5d25ae46bc85ae9db8e33e9f514af"],["/static/css/main.d2f1194c.css","757925a57837a37fe039e0095f70b4b8"],["/static/media/angry.e0c71b0d.gif","e0c71b0d9a63a362c271937e724c3709"],["/static/media/arrange.e00016a7.svg","e00016a78ac19e2702943c4777a8e957"],["/static/media/arrow.d8a03f72.svg","d8a03f72a0704b13e1cebc293e88a657"],["/static/media/bar-chart-blue.3b0e7ace.svg","3b0e7acee3faa87d450adf9fcf32bb8a"],["/static/media/bar-chart.7f7e08bc.svg","7f7e08bc867a681b300af7825ce236bc"],["/static/media/blank.ae341b94.svg","ae341b9488091b7c8a46b074d72b187b"],["/static/media/breakheart.ba3f3150.gif","ba3f3150ed6addb44ab3283b757d7828"],["/static/media/center-alignment.166fefea.svg","166fefeac4e254f71725e49f02fb83e9"],["/static/media/close.99a4879c.svg","99a4879c925fb3524a114cb2a2bdb6b5"],["/static/media/copy.ad17d20a.svg","ad17d20a524c558dea7600a239559145"],["/static/media/cry.f3b42218.gif","f3b422180360d7ac1d1822ce37d3365a"],["/static/media/dislike.311571b5.gif","311571b5720a683238fd0f332dafdf74"],["/static/media/emoji.8cc9001f.svg","8cc9001fdddbbbffb8da0bff4af1d226"],["/static/media/error.cfb42752.svg","cfb42752826cdc43aa13f87b6ca53914"],["/static/media/excited.9cb605df.gif","9cb605df26467e242e687020e9b15f5a"],["/static/media/eye.ddbd2d43.svg","ddbd2d438967d93f70f9854d1527a4a3"],["/static/media/feedback.47daf407.svg","47daf40777faf217f219fb18d1c34c66"],["/static/media/fontawesome-webfont.674f50d2.eot","674f50d287a8c48dc19ba404d20fe713"],["/static/media/fontawesome-webfont.912ec66d.svg","912ec66d7572ff821749319396470bde"],["/static/media/fontawesome-webfont.af7ae505.woff2","af7ae505a9eed503f8b8e6982036873e"],["/static/media/fontawesome-webfont.b06871f2.ttf","b06871f281fee6b241d60582ae9369b9"],["/static/media/fontawesome-webfont.fee66e71.woff","fee66e712a8a08eef5805a46892932ad"],["/static/media/haha.c6ac3dae.gif","c6ac3dae39cb127135479e979707f74b"],["/static/media/image.dd55f05d.svg","dd55f05d4f8adf75458ad78113cd335c"],["/static/media/info.9fddfc82.svg","9fddfc8231912ebcc5bc98cd5ef2820e"],["/static/media/layout.3d277c8d.svg","3d277c8d862d43bcda7e300b50f8a9c7"],["/static/media/layout1.dd3985d5.svg","dd3985d5ba0829bb33434c7c78e4c980"],["/static/media/layout2.4d14ba12.svg","4d14ba128a4d2b7fab1ef9c7095d62d5"],["/static/media/layout3.81e396e6.svg","81e396e6666fdd2f57b682d1d7cf68d7"],["/static/media/layout4.fdc26ba4.svg","fdc26ba446bf746bb1ef90f8395960cd"],["/static/media/layout5.6633d195.svg","6633d195f813e5efc6c9981789669fae"],["/static/media/layout6.3d8fe41f.svg","3d8fe41ff5d0de1ccf73b1f4eb74f5a1"],["/static/media/left-alignment.cf5b163b.svg","cf5b163be539437f6186d7726a936893"],["/static/media/left-arrows.211bf1b9.svg","211bf1b993976f60bd00008c986c00ee"],["/static/media/like.b96dd300.gif","b96dd3006475020eacbcea7cff1d2aa5"],["/static/media/line-chart-blue.dd6b7203.svg","dd6b7203323c2b8ce50cce8fcdaa559e"],["/static/media/line-chart.90639ed9.svg","90639ed9f63aef82ea10cea2722c4cf8"],["/static/media/logo.6fc4c831.svg","6fc4c831d91c3837b66b8fc111fdbba7"],["/static/media/love.773f7e20.svg","773f7e20e9435177f0976ab3367aebf6"],["/static/media/love.cb8fe944.gif","cb8fe9448dd599582bd286a19b3741dd"],["/static/media/no.031f8e35.gif","031f8e35fb655408c19e34de3225d3bf"],["/static/media/nps-blue.a2dbcd01.svg","a2dbcd01b606576f175952e62c69d6d2"],["/static/media/nps.35fb9e33.svg","35fb9e33cacc6ec8b2afde6c98274d5b"],["/static/media/package.21e45b98.svg","21e45b987d32fd8984c00e397b941a00"],["/static/media/package_white.ba890140.svg","ba8901406f5c0767a738c2a9cc2a66ff"],["/static/media/page.1fd8073e.svg","1fd8073e3c3feafcb1edddecd96441e7"],["/static/media/pie-chart-blue.262be1c3.svg","262be1c34ac75e843d405e124ba61d2c"],["/static/media/pie-chart.da63fc60.svg","da63fc60a11199fe74e02291fb639711"],["/static/media/right-alignment.f6e7a3b8.svg","f6e7a3b8e2103430238323c71f5d6b92"],["/static/media/share.4f261d30.svg","4f261d308cf238afd60d51d622fd9e1d"],["/static/media/sprite@2x.14c654cb.png","14c654cb3888e42616fa2cbccd2c40e6"],["/static/media/startpage.73dcff7b.svg","73dcff7bbabfea6e7313dc4adfeba0fc"],["/static/media/success.27bd9127.svg","27bd9127990842c7331c7fed219875ed"],["/static/media/survey.9373e83e.svg","9373e83ec0b89657581983e8d2822faf"],["/static/media/trash.097031a8.svg","097031a84bf97c7d76d670c780233bf3"],["/static/media/trash_white.3a113482.svg","3a113482f1b1cd2c0eefa3345d75a2e4"],["/static/media/up.753e4b12.svg","753e4b125935d224ecae17b28a9c778b"],["/static/media/wallet.719c4898.svg","719c489859385025efead25fd247594e"],["/static/media/warning.060bfb4a.svg","060bfb4af526ce7cf8d16363a8b91a7b"],["/static/media/wow.a75a54a6.gif","a75a54a664a47ffb23459ee4cb181975"],["/static/media/yes.0a678c1f.gif","0a678c1f47d97fcca2be9bff4b9985f2"],["/static/media/yesno.fb7ddb77.svg","fb7ddb77307e32800cc87ccf62d2ab59"]],cacheName="sw-precache-v3-sw-precache-webpack-plugin-"+(self.registration?self.registration.scope:""),ignoreUrlParametersMatching=[/^utm_/],addDirectoryIndex=function(e,a){var t=new URL(e);return"/"===t.pathname.slice(-1)&&(t.pathname+=a),t.toString()},cleanResponse=function(a){return a.redirected?("body"in a?Promise.resolve(a.body):a.blob()).then(function(e){return new Response(e,{headers:a.headers,status:a.status,statusText:a.statusText})}):Promise.resolve(a)},createCacheKey=function(e,a,t,c){var d=new URL(e);return c&&d.pathname.match(c)||(d.search+=(d.search?"&":"")+encodeURIComponent(a)+"="+encodeURIComponent(t)),d.toString()},isPathWhitelisted=function(e,a){if(0===e.length)return!0;var t=new URL(a).pathname;return e.some(function(e){return t.match(e)})},stripIgnoredUrlParameters=function(e,t){var a=new URL(e);return a.hash="",a.search=a.search.slice(1).split("&").map(function(e){return e.split("=")}).filter(function(a){return t.every(function(e){return!e.test(a[0])})}).map(function(e){return e.join("=")}).join("&"),a.toString()},hashParamName="_sw-precache",urlsToCacheKeys=new Map(precacheConfig.map(function(e){var a=e[0],t=e[1],c=new URL(a,self.location),d=createCacheKey(c,hashParamName,t,/\.\w{8}\./);return[c.toString(),d]}));function setOfCachedUrls(e){return e.keys().then(function(e){return e.map(function(e){return e.url})}).then(function(e){return new Set(e)})}self.addEventListener("install",function(e){e.waitUntil(caches.open(cacheName).then(function(c){return setOfCachedUrls(c).then(function(t){return Promise.all(Array.from(urlsToCacheKeys.values()).map(function(a){if(!t.has(a)){var e=new Request(a,{credentials:"same-origin"});return fetch(e).then(function(e){if(!e.ok)throw new Error("Request for "+a+" returned a response with status "+e.status);return cleanResponse(e).then(function(e){return c.put(a,e)})})}}))})}).then(function(){return self.skipWaiting()}))}),self.addEventListener("activate",function(e){var t=new Set(urlsToCacheKeys.values());e.waitUntil(caches.open(cacheName).then(function(a){return a.keys().then(function(e){return Promise.all(e.map(function(e){if(!t.has(e.url))return a.delete(e)}))})}).then(function(){return self.clients.claim()}))}),self.addEventListener("fetch",function(a){if("GET"===a.request.method){var e,t=stripIgnoredUrlParameters(a.request.url,ignoreUrlParametersMatching),c="index.html";(e=urlsToCacheKeys.has(t))||(t=addDirectoryIndex(t,c),e=urlsToCacheKeys.has(t));var d="/index.html";!e&&"navigate"===a.request.mode&&isPathWhitelisted(["^(?!\\/__).*"],a.request.url)&&(t=new URL(d,self.location).toString(),e=urlsToCacheKeys.has(t)),e&&a.respondWith(caches.open(cacheName).then(function(e){return e.match(urlsToCacheKeys.get(t)).then(function(e){if(e)return e;throw Error("The cached response that was expected is missing.")})}).catch(function(e){return console.warn('Couldn\'t serve response for "%s" from cache: %O',a.request.url,e),fetch(a.request)}))}});