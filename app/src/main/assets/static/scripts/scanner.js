// @ts-ignore Import module
import { HandLandmarker, FilesetResolver, DrawingUtils } from '../../ml/'; //../../lib/mediapipe/vision_bundle.js
const ort = window.ort;

var resultIconMap = {
    "A": "../../assets/img/letter-icons/a.png",
    "B": "../../assets/img/letter-icons/b.png",
    "C": "../../assets/img/letter-icons/c.png",
    "D": "../../assets/img/letter-icons/d.png",
    "E": "../../assets/img/letter-icons/e.png",
    "F": "../../assets/img/letter-icons/f.png",
    "G": "../../assets/img/letter-icons/g.png",
    "H": "../../assets/img/letter-icons/h.png",
    "I": "../../assets/img/letter-icons/i.png",
    "J": "../../assets/img/letter-icons/j.png",
    "K": "../../assets/img/letter-icons/k.png",
    "L": "../../assets/img/letter-icons/l.png",
    "M": "../../assets/img/letter-icons/m.png",
    "N": "../../assets/img/letter-icons/n.png",
    "O": "../../assets/img/letter-icons/o.png",
    "P": "../../assets/img/letter-icons/p.png",
    "Q": "../../assets/img/letter-icons/q.png",
    "R": "../../assets/img/letter-icons/r.png",
    "S": "../../assets/img/letter-icons/s.png",
    "T": "../../assets/img/letter-icons/t.png",
    "U": "../../assets/img/letter-icons/u.png",
    "V": "../../assets/img/letter-icons/v.png",
    "W": "../../assets/img/letter-icons/w.png",
    "X": "../../assets/img/letter-icons/x.png",
    "Y": "../../assets/img/letter-icons/y.png",
    "Z": "../../assets/img/letter-icons/z.png"
};

// SECTION: ELEMENTS
let video;
let canvasElt;
let canvasContext;
let enableButton;
let stopButton;
let drawing_utils;
let textBox;
let scannedLettersQueue;
let scannerNotice;
let scannerCrosshair;
let scanResultsNotice;
let btnCopyResults;
let btnClearResults;
let inputScannedLetters;
let toast;
let scannedLetterTile;
let btnToggleCrosshair;
let btnToggleReadAloud;
let resultTileIcon;
// END

// FOR ENQUEUEING SCANNED LETTERS
var utterance = null;
let synth;
var predictionBuffer = [];
var confirmationTime = 1e3;
var handFirstAppearanceTime = null;
var stabilizationDelay = 2e3;
var lastEnqueuedLetter = null;
var lastEnqueueTime = 0;
var enqueueCooldownTime = 2e3;

// FOR STATE MANAGEMENT
let scannedLettersList = [];
let shouldUseCrosshair = true;
let shouldReadAloud = true;
let isCapturing;
let isFemaleVoice = true;

const LANDMARK_CONNECTOR_COLOR = '#FFFFFF'; //'#4901FE'
const LANDMARK_POINTS_COLOR = '#FB6339'; //'#FA6127';
let currentMediaStream;
//define variables needed for mediapipe implementation
let lastTime = -1;
let results;
let handLandmarker = null;
// loading model
let session;
// wait for HandLandmarker class to finish loading
const waitForHandLandmarker = (callback) => {
    const interval = setInterval(() => {
        if (handLandmarker !== null) {
            clearInterval(interval);
            callback();
        }
    }, 100); // Check every 100ms
};
async function awaitHandLandmarker() {
    try {
        const vision = await FilesetResolver.forVisionTasks("../../assets/ml/lib/mediapipe/wasm"); //../lib/mediapipe/wasm
        // Initialize hand landmarker with additional validation
        handLandmarker = await HandLandmarker.createFromOptions(vision, {
            baseOptions: {
                modelAssetPath: ".../../assets/ml/hand_landmarker.task", //../ml/hand_landmarker.task
                delegate: "GPU",
            },
            runningMode: 'VIDEO',
            numHands: 2,
            minHandDetectionConfidence: 0.5,
            minTrackingConfidence: 0.5,
        });
        console.log("Hand Landmarker initialized successfully.");
    }
    catch (error) {
        console.error("Hand Landmarker initialization failed:", error);
    }
}
var isProcessing = false;
var fps = 15;
var intervalMs = 1e3 / fps;
var captureInterval;
function renderCapture(render) {
    if (render === true)
        captureInterval = setInterval(drawWebcamWithFrameskipping, intervalMs);
    else
        clearInterval(captureInterval);
}
function drawWebcamWithFrameskipping() {
    var _a2;
    if (isProcessing) return;
    isProcessing = true;
    if (video.videoWidth === 0 || video.videoHeight === 0) {
        isProcessing = false;
        return;
    }
    canvasElt.width = video.videoWidth;
    canvasElt.height = video.videoHeight;
    let startTime = performance.now();
    if (lastTime !== video.currentTime) {
        lastTime = video.currentTime;
        try {
            results = handLandmarker.detectForVideo(video, startTime);
        } catch (error) {
            console.error("Mediapipe detection failed: " + error);
            showResultTile(false);
            isProcessing = false;
            return;
        }
    }
    canvasContext.clearRect(0, 0, canvasElt.width, canvasElt.height);
    canvasContext.save();
    canvasContext.scale(-1, 1);
    canvasContext.translate(-canvasElt.width, 0);
    canvasContext.drawImage(video, 0, 0, canvasElt.width, canvasElt.height);
    canvasContext.restore();


    if (((_a2 = results === null || results === void 0 ? void 0 : results.landmarks) === null || _a2 === void 0 ? void 0 : _a2.length) > 0) {
        if (!handFirstAppearanceTime) {
            handFirstAppearanceTime = performance.now();
            //showTextbox("Please hold your hand steady...");
        }
        const elapsed = performance.now() - handFirstAppearanceTime;
        if (elapsed >= stabilizationDelay) {
            results.landmarks.forEach((landmarks) => {
                const mirroredLandmarks = landmarks.map((landmark) => (Object.assign(Object.assign({}, landmark), { x: 1 - landmark.x })));
                drawing_utils.drawConnectors(mirroredLandmarks, HandLandmarker.HAND_CONNECTIONS, { color: LANDMARK_CONNECTOR_COLOR, lineWidth: 1.25 });
                drawing_utils.drawLandmarks(mirroredLandmarks, { color: LANDMARK_POINTS_COLOR, radius: 2 });
                recognizeGesture(mirroredLandmarks);
            });
        } else {
            //showTextbox("Hold steady for a moment...");
        }
    } else {
        handFirstAppearanceTime = null;
        showResultTile(false);
        //showTextbox("Position your hand within the scan area.");
        predictionBuffer = [];
        lastEnqueuedLetter = null;
    }
    isProcessing = false;
}

const resizeCanvas = () => {
    const videoAspectRatio = video.videoWidth / video.videoHeight;
    const screenAspectRatio = window.innerWidth / window.innerHeight;
    if (videoAspectRatio > screenAspectRatio) {
        // Video is wider than the screen
        canvasElt.style.height = '100%';
        canvasElt.style.width = 'auto';
    }
    else {
        // Video is taller than the screen
        canvasElt.style.width = '100%';
        canvasElt.style.height = 'auto';
    }
    // console.log("Canvas resized: ", {
    //     width: canvasElt.style.width,
    //     height: canvasElt.style.height,
    // });
};
async function recognizeGesture(landmarks)
{
    if (!session) {
        console.error("ONNX model session is not loaded.");
        return;
    }

    try {
        const input = new Float32Array(42);
        landmarks.forEach((landmark, index) => {
            input[index * 2] = landmark.x;
            input[index * 2 + 1] = landmark.y;
        });
        const tensorInput = new ort.Tensor("float32", input, [1, 42]);
        const output = await session.run({ input: tensorInput });
        const predictions = output.output.data;
        const maxIndex = predictions.indexOf(Math.max(...predictions));
        const letter = String.fromCharCode(65 + maxIndex);
        const currentTime = performance.now();
        predictionBuffer.push({ letter, timestamp: currentTime });
        predictionBuffer = predictionBuffer.filter(
            // Remove stale predictions from the buffer (older than 1 second)
            (entry) => currentTime - entry.timestamp <= confirmationTime
        );
        const stablePrediction = predictionBuffer.every(
            // Check if the prediction is stable for 1 second
            (entry) => entry.letter === letter
        );
        if (stablePrediction && predictionBuffer.length > 0) {
            // hideTextbox();
            if (letter !== lastEnqueuedLetter || currentTime - lastEnqueueTime > enqueueCooldownTime) {
                lastEnqueuedLetter = letter;
                lastEnqueueTime = currentTime;
                showResultTile(true, letter);
                //enqueueResult(letter);
                enqueueScannedLetter(letter);
            }
        }
    } catch (error) {
        showResultTile(false);
        console.error("Gesture recognition failed: " + error);
    }
}
function bindEvents()
{
    window.addEventListener('resize', resizeCanvas);
    video.addEventListener('loadedmetadata', () => {
        // Ensure canvas dimensions match video
        canvasElt.width = video.videoWidth;
        canvasElt.height = video.videoHeight;
    });
    enableButton.addEventListener("click", startCapture);
    stopButton.addEventListener("click", stopCapture);
    btnCopyResults.addEventListener('click', copyToClipboard);
    btnClearResults.addEventListener('click', clearScans);
    btnToggleCrosshair.addEventListener('click', toggleCrosshair);
    btnToggleReadAloud.addEventListener('click', toggleReadAloud);
}

function toggleCrosshair() {
    shouldUseCrosshair = !shouldUseCrosshair;

    if (shouldUseCrosshair)
    {
        btnToggleCrosshair.classList.remove('off');

        if (isCapturing)
            scannerCrosshair.removeAttribute('hidden');
    }
    else
    {
        scannerCrosshair.setAttribute('hidden', '');
        btnToggleCrosshair.classList.add('off');
    }
}

function showResultTile(show, letter)
{
    if (show === true) {
        scannedLetterTile.removeAttribute('hidden');
        readLetter(letter);
        scannedLetterTile.querySelector('h6').textContent = letter;
        resultTileIcon.src = resultIconMap[letter];
    }
    else {
        scannedLetterTile.setAttribute('hidden', '');
        stopReading();
    }
}

function readLetter(letter)
{
    
}

function stopReading()
{
    
}

async function startCapture() {
    try {
        currentMediaStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: false });
        video.srcObject = currentMediaStream;
        video.play();
        scannerNotice.setAttribute('hidden', '');
        stopButton.removeAttribute('hidden');

        if (shouldUseCrosshair)
            scannerCrosshair.removeAttribute('hidden');

        isCapturing = true;
    }
    catch (err) {
        console.log("Permission denied" + err);
    }
}
function stopCapture() {
    if (currentMediaStream)
        currentMediaStream.getTracks().forEach(tracks => tracks.stop());

    video.srcObject = null;
    canvasContext.clearRect(0, 0, canvasElt.width, canvasElt.height);
    stopButton.setAttribute('hidden', '');
    scannerNotice.removeAttribute('hidden');
    scannerCrosshair.setAttribute('hidden', '');
    showResultTile(false);
    isCapturing = false;
}

function initElementReferences()
{
    video = document.getElementById("videoElement");
    canvasElt = document.getElementById("canvasFrame");
    canvasContext = canvasElt.getContext("2d");
    enableButton = document.getElementById("captureButton");
    stopButton = document.getElementById("stopButton");
    drawing_utils = new DrawingUtils(canvasContext);
    scannedLettersQueue = document.getElementById('scanned-letters-queue');
    scannerNotice = document.querySelector('#videoWrapper > #notice');
    scannerCrosshair = document.querySelector('#videoWrapper > #crosshair');
    scanResultsNotice = document.querySelector('.results-notice');
    btnClearResults = document.getElementById('btn-clear-scan-results');
    btnCopyResults = document.getElementById('btn-copy-scan-results');
    inputScannedLetters = document.getElementById('input-scanned-letters');
    scannedLetterTile = document.getElementById('scanned-letter-tile');
    btnToggleCrosshair = document.getElementById('btn-setting-toggle-xhair');
    btnToggleReadAloud = document.getElementById('btn-setting-toggle-read');
    resultTileIcon = document.getElementById('result-tile-icon');

    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl,{
            trigger : 'hover'
        })
    });
}

function clearScans() {
    scannedLettersList = [];
    inputScannedLetters.value = '';
    scannedLettersQueue.replaceChildren();
    scanResultsNotice.removeAttribute('hidden');
}

function enqueueScannedLetter(letter)
{
    if (scannedLettersQueue)
    {
        scanResultsNotice.setAttribute('hidden', '');

        // Create a new div element
        const item = document.createElement('div');

        // Add some content or attributes to the div
        item.classList.add('scanned-letters-queue-item');
        item.classList.add('resultTile');

        item.textContent = letter;

        scannedLettersQueue.appendChild(item);
        scannedLettersList.push(letter);
        inputScannedLetters.value = scannedLettersList.join('');
    }
}

function copyToClipboard()
{
    // Select the text inside the input field
    inputScannedLetters.select();
    inputScannedLetters.setSelectionRange(0, 99999); // For mobile devices
    let val = inputScannedLetters.value;

    if (val.trim() === "")
    {
        MsgBox.showWarn("Nothing to copy. Please scan a hand sign first.");
        return;
    }

    // Copy the text to the clipboard
    navigator.clipboard.writeText(val)
        .then(() => {
            // alert("Copied to clipboard: " + val);
            toast.show(`Copied to clipboard: "${val}"`, 'Copy');
        })
        .catch(err => {
            MsgBox.showError("Failed to copy: ", err);
        });
}

function initSynth()
{
    if (!window.speechSynthesis)
        return;

    synth = window.speechSynthesis;
}

async function main()
{
    initElementReferences();

    toast = new FrontendToast('#toast');
    toast.initialize();

    utterance = new SpeechSynthesisUtterance();
    utterance.lang = "en-US";
    utterance.rate = 0.65;

    initSynth();

    console.log("APP IS READY!");

    try
    {
        if (typeof ort === 'undefined') {
            throw new Error('ONNX Runtime Web is not loaded');
        }
        session = await ort.InferenceSession.create("../../assets/ml/sign_language_model.onnx");
        bindEvents();
        awaitHandLandmarker().then(() => {
            waitForHandLandmarker(() => {
                drawWebcamWithFrameskipping();
                renderCapture(true);
            });
        });
    } catch (e) {
        console.error("Failed to load ONNX model:" + e);
    }
}

window.addEventListener('load', main);