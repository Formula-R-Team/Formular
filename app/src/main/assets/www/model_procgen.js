(function() {
    function aspect() {
        return window.innerWidth / window.innerHeight;
    }
    const scene = new THREE.Scene();

    const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
    renderer.setPixelRatio(window.devicePixelRatio);
    renderer.setSize(window.innerWidth, window.innerHeight);
    renderer.setClearColor(0x000000, 0.0);
    renderer.shadowMap.enabled = true;
    renderer.shadowMap.type = THREE.PCFSoftShadowMap;

    const camera = new THREE.PerspectiveCamera(50, aspect(), 0.01, 100);
    const controls = new THREE.OrbitControls(camera, renderer.domElement);
    controls.addEventListener('change', () => {
        localStorage.setItem('camera', JSON.stringify(camera.matrix.toArray()));
        localStorage.setItem('target', JSON.stringify(controls.target.toArray()))
    });
    const cameraJson = localStorage.getItem('camera');
    if (cameraJson) {
        camera.matrix.fromArray(JSON.parse(cameraJson));
        camera.matrix.decompose(camera.position, camera.quaternion, camera.scale);
    } else {
        camera.position.set(0, 10, 0);
        camera.lookAt(new THREE.Vector3(0, 0, 0));
    }
    const targetJson = localStorage.getItem('target');
    if (targetJson) {
        controls.target.fromArray(JSON.parse(targetJson));
        controls.update();
    }
    scene.add(camera);
    scene.add(new THREE.GridHelper(10, 14, '#000000', '#636363'));

    const bezierPath = new THREE.CurvePath();
//    bezierPath.add(new THREE.CubicBezierCurve3(new THREE.Vector3(-2.324442, 0.0, -0.920311), new THREE.Vector3(-2.3295965, 0.0, -0.44455814), new THREE.Vector3(-1.963851, 0.0, -2.0658007), new THREE.Vector3(-2.773778, 0.0, -3.005762)));
//    bezierPath.add(new THREE.CubicBezierCurve3(new THREE.Vector3(-2.773778, 0.0, -3.005762), new THREE.Vector3(-3.836349, 0.0, -4.2389297), new THREE.Vector3(-7.603799, 0.0, -5.9665174), new THREE.Vector3(-4.0587106, 0.0, -7.704501)));
//    bezierPath.add(new THREE.CubicBezierCurve3(new THREE.Vector3(-4.0587106, 0.0, -7.704501), new THREE.Vector3(1.4063778, 0.0, -10.383769), new THREE.Vector3(3.8216896, 0.0, -5.9190636), new THREE.Vector3(3.3059483, 0.0, -1.1489496)));
//    bezierPath.add(new THREE.CubicBezierCurve3(new THREE.Vector3(3.3059483, 0.0, -1.1489496), new THREE.Vector3(3.1418161, 0.0, 0.36911774), new THREE.Vector3(3.1135845, 0.0, 2.8950863), new THREE.Vector3(2.2610426, 0.0, 4.204712)));
//    bezierPath.add(new THREE.CubicBezierCurve3(new THREE.Vector3(2.2610426, 0.0, 4.204712), new THREE.Vector3(0.5516529, 0.0, 6.830576), new THREE.Vector3(-4.937368, 0.0, 6.535759), new THREE.Vector3(-6.346281, 0.0, 3.7399921)));
//    bezierPath.add(new THREE.CubicBezierCurve3(new THREE.Vector3(-6.346281, 0.0, 3.7399921), new THREE.Vector3(-8.128668, 0.0, 0.20312595), new THREE.Vector3(-2.3478293, 0.0, 1.2383347), new THREE.Vector3(-2.324442, 0.0, -0.920311)));
    bezierPath.add(new THREE.CubicBezierCurve3(new THREE.Vector3(-2.324442, 0.0, -0.920311), new THREE.Vector3(-2.2956152, 0.0, -3.5809755), new THREE.Vector3(-6.929308, 0.0, -4.3908625), new THREE.Vector3(-4.9120083, 0.0, -7.0202723)));bezierPath.add(new THREE.CubicBezierCurve3(new THREE.Vector3(-4.9120083, 0.0, -7.0202723), new THREE.Vector3(-2.2822762, 0.0, -10.447944), new THREE.Vector3(3.5477629, 0.0, -8.068299), new THREE.Vector3(3.5031872, 0.0, -3.3272524)));bezierPath.add(new THREE.CubicBezierCurve3(new THREE.Vector3(3.5031872, 0.0, -3.3272524), new THREE.Vector3(3.4819403, 0.0, -1.0674572), new THREE.Vector3(3.3753414, 0.0, 3.4971805), new THREE.Vector3(1.4556942, 0.0, 4.9447823)));bezierPath.add(new THREE.CubicBezierCurve3(new THREE.Vector3(1.4556942, 0.0, 4.9447823), new THREE.Vector3(-0.6081419, 0.0, 6.5011177), new THREE.Vector3(-6.720072, 0.0, 6.387377), new THREE.Vector3(-6.5942254, 0.0, 2.674716)));bezierPath.add(new THREE.CubicBezierCurve3(new THREE.Vector3(-6.5942254, 0.0, 2.674716), new THREE.Vector3(-6.51455, 0.0, 0.32416248), new THREE.Vector3(-2.3433576, 0.0, 0.8255434), new THREE.Vector3(-2.324442, 0.0, -0.920311)));
   	scene.add(new THREE.Line(
   	    new THREE.BufferGeometry().setFromPoints(bezierPath.getPoints(bezierPath.getLength() * 16)),
   	    new THREE.LineBasicMaterial({ color: 0xffffff, depthTest: false, transparent: true, opacity: 0.75 })
    ));
    scene.add(new THREE.Points(
        new THREE.BufferGeometry().setFromPoints(bezierPath.getSpacedPoints(10)),
        new THREE.PointsMaterial({ color: 0xffffff, size: 0.125 })
    ));

    const roadHeight = 0.075;
    const roadWidth = 0.5;

    // Extrude Road Path
    const shape = new THREE.Shape();
    shape.moveTo(0, -roadWidth);
    shape.lineTo(-roadHeight, -roadWidth);
    shape.lineTo(-roadHeight, roadWidth);
    shape.lineTo(0, roadWidth);
    shape.lineTo(0, -roadWidth);
    const roadGeometry = new THREE.ExtrudeGeometry(shape, {
        steps: (bezierPath.getLength() * 6) | 0,
        extrudePath: bezierPath
    });

//   	const pathMesh = new THREE.Line(new THREE.BufferGeometry().setFromPoints(bezierPath.getPoints()), new THREE.LineBasicMaterial({ color: 0xffff00/*, depthTest: false, transparent: true*/ }));
//    pathMesh.position.setY(roadHeight);
//   	scene.add(pathMesh);
    // Extrude Road Up
    /*
   	const bezierGeom = new THREE.BufferGeometry().setFromPoints(bezierPath.getPoints().map(vec2 => new THREE.Vector3(vec2.x, 0.0, vec2.y)));
   	//scene.add(new THREE.Line(bezierGeom, new THREE.LineBasicMaterial({ color: 0xffff00, depthTest: false, transparent: true })));
    //const bezierMesh = new MeshLine();
    //bezierMesh.setGeometry(bezierGeom);
    //const surface = new THREE.Mesh(bezierMesh.geometry, new MeshLineMaterial({ color: 0x2c2f33, sizeAttenuation: true, lineWidth: 0.75, outline: true }));
    //scene.add(surface);
    const stroked = stroke(pathCurve, roadWidth);
    const shape = new THREE.Shape();
    shape.moveTo(stroked[0].firstCurve.point1.x, stroked[0].firstCurve.point1.y);
    stroked[0].curves.forEach((curve) => {
        const cp1 = curve.point1.add(curve.handle1);
        const cp2 = curve.point2.add(curve.handle2);
        shape.bezierCurveTo(cp1.x, cp1.y, cp2.x, cp2.y, curve.point2.x, curve.point2.y);
    });
    shape.closed = true;
    shape.holes.push(paperToThreePath(stroked[1]));
    //stroked.forEach(child => {
    //    const p = paperToThreePath(child);
    //    const g = new THREE.Geometry().setFromPoints(p.getPoints(50).map(vec2 => new THREE.Vector3(vec2.x, 0.0, vec2.y)));
    //    scene.add(new THREE.Line(g, new THREE.LineBasicMaterial({ color: 0x7f7f00, depthTest: false, transparent: true })));
    //});
    //const mm = new THREE.ShapeGeometry(shape);
    //mm.rotateX(0.5 * Math.PI);
    //scene.add(new THREE.Mesh(mm, new THREE.MeshBasicMaterial({ color: 0x2c2f33, side: THREE.BackSide })));
    const roadGeometry = new THREE.ExtrudeGeometry(shape, {
        bevelEnabled: false,
        depth: roadHeight
    });
    roadGeometry.rotateX(0.5 * Math.PI);
    roadGeometry.translate(0, roadHeight, 0);*/

    const tex = new THREE.TextureLoader().load('images/tile.png');
    tex.wrapS = THREE.RepeatWrapping;
    tex.wrapT = THREE.RepeatWrapping;
    tex.repeat.set(10.0, 10.0);
    tex.center.set(0.5, 0.5);
    const roadMesh = new THREE.Mesh(roadGeometry, new THREE.MeshStandardMaterial({
         color: 0xffffff,//color: 0x2c2f33,
         roughness : 0.7,
         map: tex
    }));
    roadMesh.position.set(0.0, 0.0, 0.0);
    roadMesh.receiveShadow = true;
    scene.add(roadMesh);

    const teapotSize = 0.175;
    const teapot = new THREE.Mesh(
        new THREE.TeapotBufferGeometry(teapotSize, 8),
        new THREE.MeshPhongMaterial({ side: THREE.DoubleSide })
    );
    teapot.castShadow = true;
    scene.add(teapot);
    // Lights
    scene.add(new THREE.AmbientLight(0xaaaaaa));
    const light = new THREE.DirectionalLight(0xccccff, 0.3, 100);
    light.position.set(30, 40, -60);
    light.castShadow = true;
    scene.add(light);

    window.addEventListener('resize', function() {
            camera.aspect = aspect();
            camera.updateProjectionMatrix();
            renderer.setSize(window.innerWidth, window.innerHeight);
        });
    document.body.appendChild(renderer.domElement);
    const clock = new THREE.Clock();
    let upos = 0.0;
    (function animate() {
        const delta = clock.getDelta();
        requestAnimationFrame(animate);
        //arm.rotateY(0.33 * delta);
        const point = bezierPath.getPointAt(upos);
        teapot.position.set(point.x, roadHeight + teapotSize, point.z);
        teapot.lookAt(teapot.position.clone().add(bezierPath.getTangent(upos)));
        teapot.rotateOnAxis(teapot.up, -0.5 * Math.PI);
        upos = (upos + delta * 0.1) % 1.0;
        renderer.render(scene, camera);
    })();
})();
